package com.karson.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.karson.common.utils.PageUtils;
import com.karson.mall.product.entity.SkuSaleAttrValueEntity;
import com.karson.mall.product.vo.SkuItemSalAttrsVo;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author karson
 * @email 308780714@qq.com
 * @date 2020-11-06 21:40:51
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuItemSalAttrsVo> getSaleAttrsBySpuId(Long spuId);

    List<String> getSkuSaleAttrValueAsStringList(Long skuId);
}

