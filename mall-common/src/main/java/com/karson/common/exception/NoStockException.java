package com.karson.common.exception;

/**
 * @author Karson
 */
public class NoStockException extends RuntimeException {
    private Long skuId;

    public NoStockException(String msg) {
        super(msg+":没有足够的库存了");
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}