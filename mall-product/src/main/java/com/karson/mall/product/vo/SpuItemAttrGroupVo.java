package com.karson.mall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author Karson
 */
@Data
@ToString
public class SpuItemAttrGroupVo {
    private String groupName;
    private List<SpuBaseAttrVo> attrs;
}
