package com.karson.mall.member.feign;

import com.karson.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Karson
 */
@FeignClient("mall-coupon")
public interface CouponFeignService {
    @RequestMapping("/coupon/coupon/member/list" )
    public R memberCoupons() ;
}
