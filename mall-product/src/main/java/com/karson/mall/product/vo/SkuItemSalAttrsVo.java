package com.karson.mall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author Karson
 */
@Data
@ToString
public class SkuItemSalAttrsVo {
    private Long attrId;
    private String attrName;
    private List<AttrValueWithSkuIdVo> attrValues;
}
