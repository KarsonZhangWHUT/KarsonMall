package com.karson.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.karson.common.to.mq.SecKillOrderTo;
import com.karson.common.utils.R;
import com.karson.common.vo.MemberResponseVo;
import com.karson.seckill.feign.CouponFeinService;
import com.karson.seckill.feign.ProductFeignService;
import com.karson.seckill.interceptor.LoginUserInterceptor;
import com.karson.seckill.service.SecKillService;
import com.karson.seckill.to.SecKillSkuRedisTo;
import com.karson.seckill.vo.SecKillSessionWithSkus;
import com.karson.seckill.vo.SkuInfoVo;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Karson
 */
@Service
public class SecKillServiceImpl implements SecKillService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private CouponFeinService couponFeinService;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedissonClient redissonClient;

    private final String SKU_SEMAPHORE = "seckill:stock:";//后接商品随机码

    private final String SESSIONS_PREFIX = "seckill:session:";

    private final String SKUKILL_CACHE_PREFIX = "seckill:skus";

    @Override
    public void uploadSecKillSkuLatest3Days() {
        //1.去数据库扫描最近3天需要秒杀的活动
        R latest3DaysSession = couponFeinService.getLatest3DaysSession();
        if (latest3DaysSession.getCode() == 0) {
            List<SecKillSessionWithSkus> data = latest3DaysSession.getData(new TypeReference<List<SecKillSessionWithSkus>>() {
            });
            if (data != null) {
                //将商品数据缓存到Reids
                //1.缓存活动信息
                saveSessionInfos(data);
                //2.缓存活动关联的商品信息
                saveSessionSkuInfos(data);
            }
        }
    }

    //返回当前时间可以参与的秒杀商品信息
    @Override
    public List<SecKillSkuRedisTo> getCurrentTimeSkus() {
        //确定当前时间属于哪个秒杀场次
        long time = new Date().getTime();
        Set<String> keys = stringRedisTemplate.keys(SESSIONS_PREFIX + "*");
        for (String key : keys) {
            String replace = key.replace(SESSIONS_PREFIX, "");
            String[] split = replace.split("_");
            long start = Long.parseLong(split[0]);
            long end = Long.parseLong(split[1]);
            if (time >= start && time <= end) {
                //获取这个秒杀场次的所有商品信息
                List<String> skuIds = stringRedisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                List<String> list = hashOps.multiGet(skuIds);
                if (list != null && list.size() > 0) {
                    return list.stream().map(item -> {
                        SecKillSkuRedisTo secKillSkuRedisToFromRedis = JSON.parseObject(item, new TypeReference<SecKillSkuRedisTo>() {
                        });
//                        secKillSkuRedisToFromRedis.setRandomCode(null); 当前秒杀开始了，需要随机码
                        return secKillSkuRedisToFromRedis;
                    }).collect(Collectors.toList());
                }
                break;
            }
        }
        return null;
    }

    /**
     * 获取商品的秒杀预告信息
     */
    @Override
    public SecKillSkuRedisTo getSkuSecKillInfo(Long skuId) {
        //找到所有需要参与秒杀的商品key
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);

        Set<String> keys = hashOps.keys();
        if (keys != null && keys.size() > 0) {
            String regx = "\\d_" + skuId;
            for (String key : keys) {
                //使用正则表达式匹配key是否包含商品的id
                if (Pattern.matches(regx, key)) {
                    String skuJSON = hashOps.get(key);
                    SecKillSkuRedisTo secKillSkuRedisTo = JSON.parseObject(skuJSON, SecKillSkuRedisTo.class);
                    //随机码
                    long time = new Date().getTime();
                    long startTime = secKillSkuRedisTo.getStartTime();
                    long endTime = secKillSkuRedisTo.getEndTime();
                    if (time < startTime || time > endTime) {
                        secKillSkuRedisTo.setRandomCode(null);
                    }
                    return secKillSkuRedisTo;
                }
            }
        }
        return null;
    }

    @Override
    public String kill(String killId, String key, Integer num) {

        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();

        //获取当前秒杀商品的详细信息
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        String skuJSON = hashOps.get(killId);
        if (!StringUtils.isEmpty(skuJSON)) {
            SecKillSkuRedisTo secKillSkuRedisTo = JSON.parseObject(skuJSON, SecKillSkuRedisTo.class);
            //校验合法性
            //校验时间的合法性
            long startTime = secKillSkuRedisTo.getStartTime();
            long endTime = secKillSkuRedisTo.getEndTime();
            long time = new Date().getTime();
            if (time >= startTime && time <= endTime) {
                //校验随机码和商品id是否正确
                String randomCode = secKillSkuRedisTo.getRandomCode();
                String id = secKillSkuRedisTo.getPromotionSessionId().toString() + "_" + secKillSkuRedisTo.getSkuId().toString();
                if (randomCode.equals(key) && killId.equals(id)) {
                    //随机码和商品Id和时间校验成功
                    //校验购买数量
                    if (num <= secKillSkuRedisTo.getSeckillLimit().intValue()) {
                        //验证这个人是否已经购买过了，幂等性处理，只要秒杀成功，就去占位
                        //userId skuId promotionSessionId
                        String redisKey = memberResponseVo.getId() + "_" + id;
                        //自动过期 现在时间减去结束时间  时间差就是过期时间
                        Boolean ifAbsent = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), endTime - time, TimeUnit.MILLISECONDS);
                        if (ifAbsent) {
                            //占位成功 从来没买过
                            //全部验证通过
                            //使用分布式信号量
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_SEMAPHORE + randomCode);
                            try {
                                boolean b = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);
                                if (b) {
                                    //秒杀成功
                                    //发送订单消息给MQ
                                    String orderSn = IdWorker.getTimeId();

                                    SecKillOrderTo secKillOrderTo = new SecKillOrderTo();
                                    secKillOrderTo.setOrderSn(orderSn);
                                    secKillOrderTo.setMemberId(memberResponseVo.getId());
                                    secKillOrderTo.setNum(num);
                                    secKillOrderTo.setPromotionSessionId(secKillSkuRedisTo.getPromotionSessionId());
                                    secKillOrderTo.setSeckillPrice(secKillSkuRedisTo.getSeckillPrice());
                                    secKillOrderTo.setSkuId(secKillSkuRedisTo.getSkuId());
                                    try {
                                        rabbitTemplate.convertAndSend("order-event-exchange",
                                                "order.seckill.order",
                                                secKillOrderTo);
                                    } catch (AmqpException e) {
                                        return null;
                                    }

                                    return orderSn;
                                }

                            } catch (InterruptedException e) {
                                return null;
                            }
                        }
                    }
                }
            }

        }
        return null;

    }

    private void saveSessionInfos(List<SecKillSessionWithSkus> data) {
        data.forEach(dataItem -> {
            long startTime = dataItem.getStartTime().getTime();
            long endTime = dataItem.getEndTime().getTime();
            String key = SESSIONS_PREFIX + startTime + "_" + endTime;
            Boolean hasKey = stringRedisTemplate.hasKey(key);
            if (!hasKey) {
                List<String> PromotionSessionIdWithSkuId = dataItem.getRelationSkus().stream().map(
                        item -> item.getPromotionSessionId().toString() + "_" + item.getSkuId().toString()
                ).collect(Collectors.toList());
                //缓存活动信息
                stringRedisTemplate.opsForList().leftPushAll(key, PromotionSessionIdWithSkuId);
            }
        });
    }

    private void saveSessionSkuInfos(List<SecKillSessionWithSkus> data) {
        data.forEach(dataItem -> {
            //准备hash操作结构
            BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            dataItem.getRelationSkus().forEach(secKillSkuVo -> {
                String randomCode = UUID.randomUUID().toString().replace("-", "");
                if (!hashOps.hasKey(secKillSkuVo.getPromotionSessionId() + "_" + secKillSkuVo.getSkuId().toString())) {
                    //缓存商品优惠后的基本信息
                    //1.缓存SKU的基本数据
                    //2.sku的秒杀信息
                    SecKillSkuRedisTo secKillSkuRedisTo = new SecKillSkuRedisTo();
                    //对拷秒杀数据
                    BeanUtils.copyProperties(secKillSkuVo, secKillSkuRedisTo);
                    //将商品基本信息封装到secKillSkuRedisTo中
                    R info = productFeignService.getSkuInfo(secKillSkuVo.getSkuId());
                    if (info.getCode() == 0) {
                        SkuInfoVo skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        secKillSkuRedisTo.setSkuInfoVo(skuInfo);
                    }
                    //设置上当前商品的秒杀时间信息
                    secKillSkuRedisTo.setStartTime(dataItem.getStartTime().getTime());
                    secKillSkuRedisTo.setEndTime(dataItem.getEndTime().getTime());
                    //设置商品的随机码,保护机制
                    secKillSkuRedisTo.setRandomCode(randomCode);
                    String skuJSON = JSON.toJSONString(secKillSkuRedisTo);
                    hashOps.put(secKillSkuVo.getPromotionSessionId().toString() + "_" + secKillSkuVo.getSkuId().toString(), skuJSON);

                    /**如果当前这个<b> 场次</b > 的商品的库存信息已经上架，就不需要上架了 */
//                    if (!stringRedisTemplate.hasKey(SKU_SEMAPHORE + randomCode)) {
                    //引入分布式的信号量，使用秒杀库存作为分布式的信号量
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_SEMAPHORE + randomCode);
                    semaphore.trySetPermits(secKillSkuVo.getSeckillCount().intValue());
//                    }
                }
            });
        });
    }
}
