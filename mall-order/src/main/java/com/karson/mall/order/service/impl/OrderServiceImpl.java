package com.karson.mall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.karson.common.exception.NoStockException;
import com.karson.common.to.SkuHasStockVo;
import com.karson.common.to.mq.OrderTo;
import com.karson.common.to.mq.SecKillOrderTo;
import com.karson.common.utils.PageUtils;
import com.karson.common.utils.Query;
import com.karson.common.utils.R;
import com.karson.common.vo.MemberResponseVo;
import com.karson.mall.order.constant.OrderConstant;
import com.karson.mall.order.dao.OrderDao;
import com.karson.mall.order.entity.OrderEntity;
import com.karson.mall.order.entity.OrderItemEntity;
import com.karson.mall.order.enume.OrderStatusEnum;
import com.karson.mall.order.feign.CartFeignService;
import com.karson.mall.order.feign.MemberFeignService;
import com.karson.mall.order.feign.ProductFeignService;
import com.karson.mall.order.feign.WareFeignService;
import com.karson.mall.order.interceptor.LoginUserInterceptor;
import com.karson.mall.order.service.OrderItemService;
import com.karson.mall.order.service.OrderService;
import com.karson.mall.order.to.OrderCreateTo;
import com.karson.mall.order.vo.*;
//import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //远程查询所有的收货地址列表
            List<MemberAddressVo> address = memberFeignService.getAddress(memberResponseVo.getId());
            confirmVo.setAddress(address);
        }, executor);

        CompletableFuture<Void> getCurrentItemFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //远程查询购物车所有选中的购物项
            List<OrderItemVo> currentCartItems = cartFeignService.getCurrentCartItems();
            confirmVo.setItems(currentCartItems);
        }, executor).thenRunAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> skuIds = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            List<SkuHasStockVo> skuHasStock = wareFeignService.getSkuHasStock(skuIds);
            if (skuHasStock != null) {
                Map<Long, Boolean> collect = skuHasStock.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
                confirmVo.setStocks(collect);
            }
        }, executor);


        //查询用户积分
        Integer integration = memberResponseVo.getIntegration();
        confirmVo.setIntegration(integration);
        CompletableFuture.allOf(getAddressFuture, getCurrentItemFuture).get();
        //其他数据自动计算

        //防止重复令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId(), token, 30, TimeUnit.MINUTES);

        confirmVo.setOrderToken(token);

        return confirmVo;
    }

    @Override
    @Transactional
//    @GlobalTransactional
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        responseVo.setCode(0);
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
        confirmVoThreadLocal.set(vo);
        //创建订单，验证令牌，验证价格，锁库存
        //令牌的对比和删除必须保证原子性
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();
        //lua脚本原子验证令牌和删除令牌
        Long result = stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                Collections.singletonList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId()),
                orderToken);
        if (result != null && result == 0L) {
            //令牌验证失败
            responseVo.setCode(1);
            return responseVo;
        } else {
            //令牌验证成功
            //创建订单，验证价格，锁库存
            //创建订单
            OrderCreateTo order = createOrder();
            //验价
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                //对比成功
                //保存订单
                saveOrder(order);
                //锁定库存，只要有异常，回滚保存的数据
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                wareSkuLockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> collect = order.getOrderItems().stream().map(item -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setSkuTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                wareSkuLockVo.setItemNeedLocked(collect);
                //远程锁库存
                R r = wareFeignService.orderLockStock(wareSkuLockVo);
                if (r.getCode() == 0) {
                    //锁成功
                    responseVo.setOrder(order.getOrder());
                    //订单创建成功，发送消息给MQ
                    rabbitTemplate.convertAndSend("order-event-exchange",
                            "order.create.order",
                            order.getOrder());
                    return responseVo;
                } else {
                    //失败
                    String msg = (String) r.get("msg");
                    throw new NoStockException(msg);
//                    responseVo.setCode(3);
//                    return responseVo;
                }
            } else {
                //返回
                responseVo.setCode(2);
                return responseVo;
            }
        }

//        String redisToken = stringRedisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId());
//        if (orderToken != null && orderToken.equals(redisToken)) {
//            //令牌验证通过
//            stringRedisTemplate.delete(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId());
//
//        } else {
//        }
    }

    @Override
    public OrderEntity getByOrderSn(String orderSn) {
        return this.getOne(new QueryWrapper<OrderEntity>()
                .eq("order_sn", orderSn));
    }

    @Override
    public void closeOrder(OrderEntity entity) {
        //关闭订单之前查询这个订单最新状态
        OrderEntity orderEntity = this.getById(entity.getId());
        if (orderEntity != null && orderEntity.getStatus().equals(OrderStatusEnum.CREATE_NEW.getCode())) {
            OrderEntity update = new OrderEntity();
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            update.setId(entity.getId());
            this.updateById(update);
            //订单解锁成功，发给MQ一个
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);
            try {
                //保证消息一定会发出去，每一个消息都可以做好日志记录
                rabbitTemplate.convertAndSend("order-event-exchange",
                        "order.release.other",
                        orderEntity);
            } catch (AmqpException e) {
                //将没发送成功的消息进行重试发送

            }
        }
    }

    @Override
    public void createSeckillOrder(SecKillOrderTo secKillOrder) {
        //保存订单信息
        OrderEntity orderEntity = new OrderEntity();

        orderEntity.setOrderSn(secKillOrder.getOrderSn());
        orderEntity.setMemberId(secKillOrder.getMemberId());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        BigDecimal multiply = secKillOrder.getSeckillPrice().multiply(new BigDecimal(secKillOrder.getNum()));
        orderEntity.setPayAmount(multiply);
        save(orderEntity);

        //保存订单项信息
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderId(orderEntity.getId());
        orderItemEntity.setOrderSn(orderEntity.getOrderSn());
        orderItemEntity.setRealAmount(multiply);
        orderItemEntity.setSkuQuantity(secKillOrder.getNum());
        //TODO 更多详细信息不设置了
        orderItemService.save(orderItemEntity);
    }

    /**
     * 保存订单数据
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);

        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    private OrderCreateTo createOrder() {
        OrderCreateTo createTo = new OrderCreateTo();
        //生成订单号
        String orderSn = IdWorker.getTimeId();
        OrderEntity orderEntity = buildOrder(orderSn);

        //获取到订单项
        List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);

        //验价,计算价格相关
        computePrice(orderEntity, itemEntities);
        createTo.setOrder(orderEntity);
        createTo.setOrderItems(itemEntities);
        return createTo;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {
        //订单价格相关
        BigDecimal total = new BigDecimal(0);

        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");
        BigDecimal gift = new BigDecimal("0.0");
        BigDecimal growth = new BigDecimal("0.0");
        for (OrderItemEntity itemEntity : itemEntities) {
            BigDecimal realAmount = itemEntity.getRealAmount();
            coupon = coupon.add(itemEntity.getCouponAmount());
            integration = integration.add(itemEntity.getIntegrationAmount());
            promotion = promotion.add(itemEntity.getPromotionAmount());
            // 购物获取的积分、成长值
            gift = gift.add(new BigDecimal(itemEntity.getGiftIntegration().toString()));
            growth = growth.add(new BigDecimal(itemEntity.getGiftGrowth().toString()));
            total = total.add(realAmount);
        }
        //订单的总额
        orderEntity.setTotalAmount(total);
        //订单的应付总额
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));

        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setCouponAmount(coupon);

        // 设置积分、成长值
        orderEntity.setIntegration(gift.intValue());
        orderEntity.setGrowth(growth.intValue());

        // 设置订单的删除状态
        orderEntity.setDeleteStatus(0);
    }

    private OrderEntity buildOrder(String orderSn) {
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);
        orderEntity.setMemberId(memberResponseVo.getId());//会员id

        //远程获取收货地址信息
        OrderSubmitVo orderSubmitVo = confirmVoThreadLocal.get();
        R fare = wareFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareResp = fare.getData(new TypeReference<FareVo>() {
        });
        //设置运费信息
        orderEntity.setFreightAmount(fareResp.getFare());
        //设置收货人信息
        orderEntity.setReceiverCity(fareResp.getAddress().getCity());
        orderEntity.setReceiverDetailAddress(fareResp.getAddress().getDetailAddress());
        orderEntity.setReceiverName(fareResp.getAddress().getName());
        orderEntity.setReceiverPhone(fareResp.getAddress().getPhone());
        orderEntity.setReceiverPostCode(fareResp.getAddress().getPostCode());
        orderEntity.setReceiverProvince(fareResp.getAddress().getProvince());
        orderEntity.setReceiverRegion(fareResp.getAddress().getRegion());

        //设置订单状态
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setAutoConfirmDay(7);

        return orderEntity;
    }

    /**
     * 构建订单项数据
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        //最后确定每个购物项的价格
        List<OrderItemVo> currentCartItems = cartFeignService.getCurrentCartItems();
        if (currentCartItems != null && currentCartItems.size() > 0) {
            List<OrderItemEntity> itemEntities = currentCartItems.stream().map(cartItem -> {
                OrderItemEntity orderItemEntity = buildOrderItem(cartItem);
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
            return itemEntities;
        }
        return null;
    }

    /**
     * 构建订单项
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        //商品的spu信息
        Long skuId = cartItem.getSkuId();
        R spuInfoBySkuId = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoEntity spuInfoEntity = spuInfoBySkuId.getData(new TypeReference<SpuInfoEntity>() {
        });
        orderItemEntity.setSpuId(spuInfoEntity.getId());
        orderItemEntity.setSpuName(spuInfoEntity.getSpuName());
        orderItemEntity.setSpuBrand(spuInfoEntity.getBrandId().toString());
        orderItemEntity.setCategoryId(spuInfoEntity.getCatalogId());

        //商品的sku信息
        orderItemEntity.setSkuId(cartItem.getSkuId());
        orderItemEntity.setSkuName(cartItem.getSkuTitle());
        orderItemEntity.setSkuPic(cartItem.getImage());
        orderItemEntity.setSkuPrice(cartItem.getPrice());
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttr);
        orderItemEntity.setSkuQuantity(cartItem.getCount());
        //优惠信息 不做

        //积分信息
        orderItemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue());
        orderItemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue());

        //订单项的价格信息
        orderItemEntity.setPromotionAmount(new BigDecimal(0));
        orderItemEntity.setCouponAmount(new BigDecimal(0));
        orderItemEntity.setIntegrationAmount(new BigDecimal(0));
        //当前订单项的实际金额
        BigDecimal totalPrice = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        BigDecimal realPrice = totalPrice.subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(realPrice);
        return orderItemEntity;
    }

}