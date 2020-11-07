package com.karson.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.karson.common.utils.PageUtils;
import com.karson.mall.product.entity.AttrEntity;

import java.util.Map;

/**
 * 商品属性
 *
 * @author karson
 * @email 308780714@qq.com
 * @date 2020-11-06 21:40:52
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

