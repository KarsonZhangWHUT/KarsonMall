package com.karson.mall.ware.feign;

import com.karson.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Karson
 */
@FeignClient("mall-product")
public interface ProductFeignService {
    /*
        让所有请求过网关：@FeignClient("mall-gateway")
        @RequestMapping("/api/product/spuinfo/info/{id}")

        请求不过网关：@FeignClient("mall-product")
        @RequestMapping("/product/spuinfo/info/{id}")
     */
    @RequestMapping("/product/skuInfo/info/{skuInfo}")
    public R info(@PathVariable("skuId") Long skuId);
}
