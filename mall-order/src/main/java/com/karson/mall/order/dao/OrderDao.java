package com.karson.mall.order.dao;

import com.karson.mall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author karson
 * @email 308780714@qq.com
 * @date 2020-11-07 16:18:37
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
