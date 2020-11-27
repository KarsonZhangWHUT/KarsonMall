package com.karson.seckill.to;

import com.karson.seckill.vo.SkuInfoVo;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Karson
 */
@Data
public class SecKillSkuRedisTo {

    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;


    /**
     * 秒杀随机码
     */
    private String randomCode;

    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private BigDecimal seckillCount;
    /**
     * 每人限购数量
     */
    private BigDecimal seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;

    //SKU的详细信息

    private SkuInfoVo skuInfoVo;

    //商品秒杀开始时间
    private long startTime;
    //商品秒杀结束时间
    private long endTime;
}
