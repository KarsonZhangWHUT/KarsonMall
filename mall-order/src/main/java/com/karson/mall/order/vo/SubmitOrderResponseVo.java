package com.karson.mall.order.vo;

import com.karson.mall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @author Karson
 */
@Data
public class SubmitOrderResponseVo {
    private OrderEntity order;
    private Integer code;//0成功

}
