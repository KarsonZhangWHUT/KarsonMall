package com.karson.mall.product.feign;

import com.karson.common.to.SkuReductionTo;
import com.karson.common.to.SpuBoundsTo;
import com.karson.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Karson
 */
@FeignClient("mall-coupon")
public interface CouponFeignService {

    /**
     * 1. @RequestBody 将这个对象转为json
     * 2. 找到mall-coupon服务，给/coupon/spubounds/save发送请求，
     * 将上一步生成的json放在请求体位置，发送请求
     * 3. 对方服务收到请求。请求体里有json数据。
     * (两个服务传输的json数据只要属性名相同就能封装)
     * <p>
     * 只要json数据模型是兼容的,双方服务无需使用同一个to
     */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundsTo spuBoundsTo);

    @PostMapping("/coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
