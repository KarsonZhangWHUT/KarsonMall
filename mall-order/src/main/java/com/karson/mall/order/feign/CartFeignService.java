package com.karson.mall.order.feign;

import com.karson.mall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author Karson
 */
@FeignClient("mall-cart")
public interface CartFeignService {
    @GetMapping("/currentCartItems")
    List<OrderItemVo> getCurrentCartItems();
}
