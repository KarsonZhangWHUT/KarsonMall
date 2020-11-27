package com.karson.mall.ware.vo;

import lombok.Data;

/**
 * @author Karson
 */
@Data
public class LockStockResult {
    private Long skuId;
    private Integer number;
    public boolean locked;

}
