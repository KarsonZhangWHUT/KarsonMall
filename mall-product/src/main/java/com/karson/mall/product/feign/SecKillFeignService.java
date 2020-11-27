package com.karson.mall.product.feign;

import com.karson.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author Karson
 */
@FeignClient("mall-seckill")
public interface SecKillFeignService {
    @GetMapping("/sku/seckill/{skuId}")
    R getSkuSecKillInfo(@PathVariable("skuId") Long skuId);
}
