package com.karson.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.karson.common.utils.PageUtils;
import com.karson.mall.product.entity.BrandEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌
 *
 * @author karson
 * @email 308780714@qq.com
 * @date 2020-11-06 21:40:52
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void updateDetail(BrandEntity brandEntity);

    List<BrandEntity> getBrandsByIds(List<Long> brandIds);
}

