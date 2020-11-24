package com.karson.mall.search.controller;

import com.karson.common.exception.BizCodeEnum;
import com.karson.common.to.es.SkuESModel;
import com.karson.common.utils.R;
import com.karson.mall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * @author Karson
 */
@RestController
@RequestMapping("/search/save")
@Slf4j
public class ElasticSaveController {

    @Autowired
    private ProductSaveService productSaveService;

    /**
     * 商家商品
     */
    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuESModel> skuESModels) {
        boolean b = false;
        try {
            b = productSaveService.productStatusUp(skuESModels);
        } catch (IOException e) {
            log.error("ElasticSaveController商品上架错误：{}", e.getMessage());
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());
        }
        if (b)
            return R.ok();
        else
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());

    }

}
