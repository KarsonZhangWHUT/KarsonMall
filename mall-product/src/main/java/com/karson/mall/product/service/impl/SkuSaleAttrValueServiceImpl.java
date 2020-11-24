package com.karson.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.karson.common.utils.PageUtils;
import com.karson.common.utils.Query;
import com.karson.mall.product.dao.SkuSaleAttrValueDao;
import com.karson.mall.product.entity.SkuSaleAttrValueEntity;
import com.karson.mall.product.service.SkuSaleAttrValueService;
import com.karson.mall.product.vo.SkuItemSalAttrsVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuItemSalAttrsVo> getSaleAttrsBySpuId(Long spuId) {

        SkuSaleAttrValueDao skuSaleAttrValueDao = this.baseMapper;
        return skuSaleAttrValueDao.getSaleAttrsBySpuId(spuId);
    }

    @Override
    public List<String> getSkuSaleAttrValueAsStringList(Long skuId) {
        SkuSaleAttrValueDao attrValueDao = this.baseMapper;
        return attrValueDao.getSkuSaleAttrValueAsStringList(skuId);
    }

}