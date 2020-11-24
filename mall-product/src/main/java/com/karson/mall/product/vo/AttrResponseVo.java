package com.karson.mall.product.vo;

import lombok.Data;

/**
 * @author Karson
 */
@Data
public class AttrResponseVo extends AttrVo {

    private String catelogName;
    private String groupName;

    private Long[] catelogPath;
}
