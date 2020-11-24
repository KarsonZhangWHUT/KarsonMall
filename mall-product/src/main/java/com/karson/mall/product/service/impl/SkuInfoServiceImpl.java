package com.karson.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.karson.common.utils.PageUtils;
import com.karson.common.utils.Query;
import com.karson.mall.product.dao.SkuInfoDao;
import com.karson.mall.product.entity.SkuImagesEntity;
import com.karson.mall.product.entity.SkuInfoEntity;
import com.karson.mall.product.entity.SpuInfoDescEntity;
import com.karson.mall.product.service.*;
import com.karson.mall.product.vo.SkuItemSalAttrsVo;
import com.karson.mall.product.vo.SkuItemVo;
import com.karson.mall.product.vo.SpuItemAttrGroupVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {


    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    /*
    key:
    catelogId: 0
    brandId: 0
    min: 0
    max: 0
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> {
                w.eq("sku_id", key).or().like("sku_name", key);
            });
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }
        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            wrapper.ge("price", min);
        }
        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max)) {

            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if (bigDecimal.compareTo(new BigDecimal(0)) > 0)
                    wrapper.le("price", max);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        return this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
    }

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            //1sku基本属性获取
            SkuInfoEntity skuInfoEntity = getById(skuId);
            skuItemVo.setInfo(skuInfoEntity);
            return skuInfoEntity;
        }, threadPoolExecutor);

        CompletableFuture<Void> saleAttrFutrue = infoFuture.thenAcceptAsync((res) -> {
            //3获取spu的销售属性组合
            List<SkuItemSalAttrsVo> saleAttrsVos = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            skuItemVo.setSalAttr(saleAttrsVos);
        }, threadPoolExecutor);

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((res) -> {
            //4获取spu的介绍
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            System.out.println(spuInfoDescEntity);
            skuItemVo.setDesp(spuInfoDescEntity);
        }, threadPoolExecutor);

        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            //5获取spu的规格参数信息
            List<SpuItemAttrGroupVo> attrGroupAttrs = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(attrGroupAttrs);
        }, threadPoolExecutor);

//        //1sku基本属性获取
//        Long catalogId = skuInfoEntity.getCatalogId();
//        Long spuId = skuInfoEntity.getSpuId();

//        //2sku的图片信息
//        List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
//        skuItemVo.setImages(images);
//        //3获取spu的销售属性组合
//        List<SkuItemSalAttrsVo> saleAttrsVos = skuSaleAttrValueService.getSaleAttrsBySpuId(spuId);
//        skuItemVo.setSalAttr(saleAttrsVos);

//        //4获取spu的介绍
//        SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(spuId);
//        skuItemVo.setDesp(spuInfoDescEntity);

//        //5获取spu的规格参数信息
//        List<SpuItemAttrGroupVo> attrGroupAttrs = attrGroupService.getAttrGroupWithAttrsBySpuId(spuId, catalogId);
//        skuItemVo.setGroupAttrs(attrGroupAttrs);

        CompletableFuture<Void> imagesFuture = CompletableFuture.runAsync(() -> {
            //2sku的图片信息
            List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(images);
        }, threadPoolExecutor);

        CompletableFuture.allOf(descFuture, saleAttrFutrue, baseAttrFuture, imagesFuture).get();

        //等待所有任务都完成
        return skuItemVo;
    }

}