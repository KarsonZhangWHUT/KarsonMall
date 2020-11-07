package com.karson.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.karson.common.utils.PageUtils;
import com.karson.mall.order.entity.OrderEntity;

import java.util.Map;

/**
 * 订单
 *
 * @author karson
 * @email 308780714@qq.com
 * @date 2020-11-07 16:18:37
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

