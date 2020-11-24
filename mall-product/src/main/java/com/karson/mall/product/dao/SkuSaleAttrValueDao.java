package com.karson.mall.product.dao;

import com.karson.mall.product.entity.SkuSaleAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.karson.mall.product.vo.SkuItemSalAttrsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author karson
 * @email 308780714@qq.com
 * @date 2020-11-06 21:40:51
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    List<SkuItemSalAttrsVo> getSaleAttrsBySpuId(@Param("spuId") Long spuId);

    List<String> getSkuSaleAttrValueAsStringList(@Param("skuId") Long skuId);
}
