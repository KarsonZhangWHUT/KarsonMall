package com.karson.mall.order.dao;

import com.karson.mall.order.entity.RefundInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 退款信息
 * 
 * @author karson
 * @email 308780714@qq.com
 * @date 2020-11-07 16:18:36
 */
@Mapper
public interface RefundInfoDao extends BaseMapper<RefundInfoEntity> {
	
}
