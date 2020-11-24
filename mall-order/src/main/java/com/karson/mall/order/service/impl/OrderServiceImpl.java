package com.karson.mall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.karson.common.utils.PageUtils;
import com.karson.common.utils.Query;
import com.karson.common.vo.MemberResponseVo;
import com.karson.mall.order.dao.OrderDao;
import com.karson.mall.order.entity.OrderEntity;
import com.karson.mall.order.feign.CartFeignService;
import com.karson.mall.order.feign.MemberFeignService;
import com.karson.mall.order.interceptor.LoginUserInterceptor;
import com.karson.mall.order.service.OrderService;
import com.karson.mall.order.vo.MemberAddressVo;
import com.karson.mall.order.vo.OrderConfirmVo;
import com.karson.mall.order.vo.OrderItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private CartFeignService cartFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
        //远程查询所有的收货地址列表
        List<MemberAddressVo> address = memberFeignService.getAddress(memberResponseVo.getId());
        confirmVo.setAddress(address);
        //远程查询购物车所有选中的购物项
        List<OrderItemVo> currentCartItems = cartFeignService.getCurrentCartItems();
        confirmVo.setItems(currentCartItems);

        //查询用户积分
        Integer integration = memberResponseVo.getIntegration();
        confirmVo.setIntegration(integration);

        //其他数据自动计算
        return null;
    }

}