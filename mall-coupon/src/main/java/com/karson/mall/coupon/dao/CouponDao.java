package com.karson.mall.coupon.dao;

import com.karson.mall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author karson
 * @email 308780714@qq.com
 * @date 2020-11-07 15:53:17
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
