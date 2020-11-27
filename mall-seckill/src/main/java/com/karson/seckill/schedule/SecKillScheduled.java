package com.karson.seckill.schedule;

import com.karson.seckill.service.SecKillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author Karson
 * 秒杀商品的定时上架
 * 每天晚上3点 上架最近3天需要秒杀的商品
 */
@Slf4j
@Service
public class SecKillScheduled {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private SecKillService secKillService;

    private final String uploadLock = "seckill:upload:lock";

    @Scheduled(cron = "*/3 * * * * ?")
    public void uploadSecKillSkuLatest3Days() {
        //重复上架无需处理
        log.info("上架秒杀的商品信息");
        //分布式锁 锁的业务执行完成，状态已经更新完成，释放锁以后，其他获取到的就会拿到最新的状态
        RLock lock = redissonClient.getLock(uploadLock);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            secKillService.uploadSecKillSkuLatest3Days();
        } finally {
            lock.unlock();
        }
    }
}
