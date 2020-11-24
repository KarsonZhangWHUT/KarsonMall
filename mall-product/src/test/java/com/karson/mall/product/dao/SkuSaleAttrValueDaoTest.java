package com.karson.mall.product.dao;

import com.karson.mall.product.vo.SkuItemSalAttrsVo;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Karson
 */
@SpringBootTest
class SkuSaleAttrValueDaoTest {

    @Resource
    SkuSaleAttrValueDao skuSaleAttrValueDao;
    @Test
    void getSaleAttrsBySpuId() {
        List<SkuItemSalAttrsVo> saleAttrsBySpuId = skuSaleAttrValueDao.getSaleAttrsBySpuId(3L);
        System.out.println(saleAttrsBySpuId);
    }
}