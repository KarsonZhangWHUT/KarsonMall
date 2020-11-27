package com.karson.mall.order.to;

import com.karson.mall.order.entity.OrderEntity;
import com.karson.mall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Karson
 */
@Data
public class OrderCreateTo {
    private OrderEntity order;
    private List<OrderItemEntity> orderItems;
    private BigDecimal payPrice;//订单计算的应付价格
    private BigDecimal fare;
}
