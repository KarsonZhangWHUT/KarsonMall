package com.karson.common.to.mq;

import lombok.Data;

/**
 * @author Karson
 */
@Data
public class StockLockedTo {
    private Long id;//库存工作单的id
    private StockDetailTo stockDetailTo;//工作单的详情
}
