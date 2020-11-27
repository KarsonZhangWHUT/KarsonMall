package com.karson.seckill.feign;

import com.karson.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Karson
 */
@FeignClient("mall-coupon")
public interface CouponFeinService {

    @GetMapping("/coupon/seckillsession/latest3DaySession")
    R getLatest3DaysSession();
}
