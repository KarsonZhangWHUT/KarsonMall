package com.karson.seckill.service;

import com.karson.seckill.to.SecKillSkuRedisTo;

import java.util.List;

/**
 * @author Karson
 */
public interface SecKillService {
    void uploadSecKillSkuLatest3Days();

    List<SecKillSkuRedisTo> getCurrentTimeSkus();

    SecKillSkuRedisTo getSkuSecKillInfo(Long skuId);

    String kill(String killId, String key, Integer num);
}
