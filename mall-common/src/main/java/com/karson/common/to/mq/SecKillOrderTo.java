package com.karson.common.to.mq;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Karson
 */
@Data
public class SecKillOrderTo {
    private String orderSn;
    private Long promotionSessionId;
    private Long skuId;
    private BigDecimal seckillPrice; //秒杀价格
    private Integer num;
    private Long memberId;
}
