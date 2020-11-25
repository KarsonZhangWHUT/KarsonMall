package com.karson.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Karson
 */
@Data
public class OrderConfirmVo {

    //收货地址
    private List<MemberAddressVo> address;

    //所有选中的购物项

    private List<OrderItemVo> items;

    //优惠信息
    private Integer integration;

    private String orderToken;


    private Map<Long,Boolean> stocks;

    //订单总额
//    private BigDecimal total;

    //应付价格
//    private BigDecimal payPrice;


    public Integer getTotalCount() {
        int i = 0;
        if (items != null && items.size() > 0) {
            for (OrderItemVo item : items) {
                i = i + item.getCount();
            }
        }
        return i;
    }


    public BigDecimal getPayPrice() {
        return getTotal();

    }

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal(0);
        if (items != null && items.size() > 0)
            for (OrderItemVo item : items) {
                sum = sum.add(item.getPrice().multiply(new BigDecimal(item.getCount())));
            }
        return sum;
    }


}
