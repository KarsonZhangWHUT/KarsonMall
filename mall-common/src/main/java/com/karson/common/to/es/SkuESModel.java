package com.karson.common.to.es;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Karson
 * sku在es中的模型
 */
@Data
public class SkuESModel {
    private Long skuId;
    private Long spuId;
    private String skuTitle;
    private BigDecimal skuPrice;//
    private String skuImage;//
    private Long saleCount;
    private Boolean hasStock;//??
    private Long hotScore;//??
    private Long brandId;
    private Long catalogId;
    private String brandName;//??
    private String brandImg;//??
    private String catalogName;//??
    private List<Attrs> attrs;//??

    @Data
    public static class Attrs {
        private Long attrId;
        private String attrName;
        private String attrValue;
    }
}
