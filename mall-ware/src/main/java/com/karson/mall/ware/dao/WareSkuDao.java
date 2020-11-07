package com.karson.mall.ware.dao;

import com.karson.mall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品库存
 * 
 * @author karson
 * @email 308780714@qq.com
 * @date 2020-11-07 16:23:45
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {
	
}
