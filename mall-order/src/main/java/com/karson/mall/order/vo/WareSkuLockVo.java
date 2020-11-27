package com.karson.mall.order.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Karson
 */
@Data
public class WareSkuLockVo {
    private String orderSn;
    private List<OrderItemVo> itemNeedLocked;

}
