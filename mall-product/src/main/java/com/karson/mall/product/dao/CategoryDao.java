package com.karson.mall.product.dao;

import com.karson.mall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author karson
 * @email 308780714@qq.com
 * @date 2020-11-06 21:40:52
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
