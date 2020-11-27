package com.karson.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.karson.common.to.mq.SecKillOrderTo;
import com.karson.common.utils.PageUtils;
import com.karson.mall.order.entity.OrderEntity;
import com.karson.mall.order.vo.OrderConfirmVo;
import com.karson.mall.order.vo.OrderSubmitVo;
import com.karson.mall.order.vo.SubmitOrderResponseVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author karson
 * @email 308780714@qq.com
 * @date 2020-11-07 16:18:37
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 订单确认页返回需要用的数据
     */
    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);

    OrderEntity getByOrderSn(String orderSn);

    void closeOrder(OrderEntity entity);

    void createSeckillOrder(SecKillOrderTo secKillOrder);
}

