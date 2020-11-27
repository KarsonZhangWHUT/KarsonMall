package com.karson.mall.order.vo;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @author Karson
 * 封装订单页面提交的数据
 */
@Data
@ToString
public class OrderSubmitVo {
    private Long addrId;//收货地址的id
    private Integer payType;//支付方式
    //无需提交购买的商品，去购物车再获取一遍
    //优惠，发票没实现，不做
    private String orderToken;//防重复令牌
    private BigDecimal payPrice;//验价
    //用户相关信息都在session里面，提交请求的时候前端会带上cookie，就会在后端验证到是哪个用户
    private String note;//订单备注
}
