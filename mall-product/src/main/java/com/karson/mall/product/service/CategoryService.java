package com.karson.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.karson.common.utils.PageUtils;
import com.karson.mall.product.entity.CategoryEntity;
import com.karson.mall.product.vo.Catalog2Vo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author karson
 * @email 308780714@qq.com
 * @date 2020-11-06 21:40:52
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIds(List<Long> asList);

    /**
     * 找到catelogId的完整路径
     *
     */
    Long[] findCatelogPath(Long catelogId);

    void updateCascade(CategoryEntity category);

    List<CategoryEntity> getLevel1Category();

    Map<String, List<Catalog2Vo>> getCatalogJson();
}

