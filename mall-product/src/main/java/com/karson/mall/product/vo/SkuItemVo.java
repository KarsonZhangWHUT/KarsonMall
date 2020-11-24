package com.karson.mall.product.vo;

import com.karson.mall.product.entity.SkuImagesEntity;
import com.karson.mall.product.entity.SkuInfoEntity;
import com.karson.mall.product.entity.SpuInfoDescEntity;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author Karson
 */

@Data
@ToString
public class SkuItemVo {
    //sku基本属性获取
    private SkuInfoEntity info;

    private boolean hasStock=true;

    //sku的图片信息
    private List<SkuImagesEntity> images;

    //获取spu的销售属性组合
    List<SkuItemSalAttrsVo> salAttr;

    //获取spu的介绍
    private SpuInfoDescEntity desp;


    //获取spu的规格参数信息
    private List<SpuItemAttrGroupVo> groupAttrs;


}
