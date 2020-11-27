package com.karson.mall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Karson
 */
@Data
public class OrderItemVo {
    private Long skuId;
    private String skuTitle;
    private String image;
    private List<String> skuAttr;
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;

    //TODO 查询库存状态
    private boolean hasStock;
}
