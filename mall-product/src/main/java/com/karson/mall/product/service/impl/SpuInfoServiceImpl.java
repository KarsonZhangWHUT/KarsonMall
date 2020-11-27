package com.karson.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.karson.common.constant.ProductConstant;
import com.karson.common.to.SkuHasStockVo;
import com.karson.common.to.SkuReductionTo;
import com.karson.common.to.SpuBoundsTo;
import com.karson.common.to.es.SkuESModel;
import com.karson.common.utils.PageUtils;
import com.karson.common.utils.Query;
import com.karson.common.utils.R;
import com.karson.mall.product.dao.SpuInfoDao;
import com.karson.mall.product.entity.*;
import com.karson.mall.product.feign.CouponFeignService;
import com.karson.mall.product.feign.SearchFeignService;
import com.karson.mall.product.feign.WareFeignService;
import com.karson.mall.product.service.*;
import com.karson.mall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {


    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void saveSpuInfo(SpuSaveVo spuSaveVo) {

        //1.保存spu基本信息pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);

        //2.保存spu的描述图片pms_spu_info_desc
        List<String> decript = spuSaveVo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",", decript));
        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);

        //3.保存spu的图片集pms_spu_images
        List<String> images = spuSaveVo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(), images);

        //4.保存spu的规格参数; pms_product_attr_value
        List<BaseAttrs> baseAttrs = spuSaveVo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setAttrId(attr.getAttrId());
            AttrEntity attrEntity = attrService.getById(attr.getAttrId());
            productAttrValueEntity.setAttrName(attrEntity.getAttrName());
            productAttrValueEntity.setAttrValue(attr.getAttrValues());
            productAttrValueEntity.setQuickShow(attr.getShowDesc());
            productAttrValueEntity.setSpuId(spuInfoEntity.getId());
            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttr(collect);

        //保存spu的积分信息mall_sms.sms_spu_bounds
        Bounds bounds = spuSaveVo.getBounds();
        SpuBoundsTo spuBoundsTo = new SpuBoundsTo();
        BeanUtils.copyProperties(bounds, spuBoundsTo);
        spuBoundsTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundsTo);
        if (r.getCode() != 0) {
            log.error("远程保存spu积分信息失败");
        }

        //5.保存当前对应的所有sku信息
        List<Skus> skus = spuSaveVo.getSkus();
        if (skus != null && skus.size() > 0) {
//            private String skuName;
//            private BigDecimal price;
//            private String skuTitle;
//            private String skuSubtitle;
            skus.forEach(sku -> {
                String defaultImage = "";
                for (Images image : sku.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImage = image.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImage);
                //5.1sku的基本信息pms_sku_info
                skuInfoService.saveSkuInfo(skuInfoEntity);
                Long skuId = skuInfoEntity.getSkuId();


                List<SkuImagesEntity> skuImagesEntities = sku.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity -> {
                    //返回true就是需要，返回false就剔除
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                //5.2sku的图片信息pms_sku_images
                skuImagesService.saveBatch(skuImagesEntities);
                //5.3sku的销售属性信息pms_sku_sale_attr_value
                List<Attr> attrs = sku.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attrs.stream().map(attr -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);
                //5.4sku的优惠信息满减信息mall_sms.sms_sku_ladder\sms_sku_full_reduction\sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(sku, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal(0)) > 0) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存spu优惠信息失败");
                    }
                }
            });
        }
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> {
                w.eq("id", key).or().like("spu_name", key);
            });
        }
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("publish_status", status);

        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq("catalog_id", catelogId);

        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {

        //查出当前spuid对应的所有sku信息，品牌的名字
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIdList = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        //DONETODO 查询当前SKU的所有可以被用来检索的规格属性
        List<ProductAttrValueEntity> attrValueEntities = productAttrValueService.baseAttrListForSpu(spuId);
        List<Long> list = attrValueEntities.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
        List<Long> searchAttrIds = attrService.selectSearchAttrsIds(list);
        Set<Long> idSet = new HashSet<>(searchAttrIds);
        List<SkuESModel.Attrs> attrsList = attrValueEntities.stream().filter(item -> {
            return idSet.contains(item.getAttrId());
        }).map(item -> {
            SkuESModel.Attrs attrs1 = new SkuESModel.Attrs();
            BeanUtils.copyProperties(item, attrs1);
            return attrs1;
        }).collect(Collectors.toList());

        Map<Long, Boolean> stockMap = null;
        try {
            //DONETODO 发送远程调用，库存系统查询是否有库存
            List<SkuHasStockVo> skuHasStock = wareFeignService.getSkuHasStock(skuIdList);
            if (skuHasStock != null)
                stockMap = skuHasStock.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
        } catch (Exception e) {
            log.error("库存服务查询异常，原因：{}", e);
        }

        //stockMap可能为null
        if (stockMap != null) {
            //封装每个sku的信息
            Map<Long, Boolean> finalStockMap = stockMap;
            List<SkuESModel> upProduct = skus.stream().map(sku -> {
                //组装需要的数据
                SkuESModel esModel = new SkuESModel();
                BeanUtils.copyProperties(sku, esModel);
                esModel.setSkuPrice(sku.getPrice());
                esModel.setSkuImage(sku.getSkuDefaultImg());
                //设置库存信息
                if (finalStockMap == null) {
                    esModel.setHasStock(true);
                } else {
                    esModel.setHasStock(finalStockMap.get(sku.getSkuId()));
                }
                //DONETODO 热度评分  默认0
                esModel.setHotScore(0L);
                //DONETODO 品牌和分类的名字信息
                BrandEntity brandEntity = brandService.getById(esModel.getBrandId());
                esModel.setBrandName(brandEntity.getName());
                esModel.setBrandImg(brandEntity.getLogo());
                CategoryEntity categoryEntity = categoryService.getById(esModel.getCatalogId());
                esModel.setCatalogName(categoryEntity.getName());
                //设置检索属性
                esModel.setAttrs(attrsList);
                return esModel;
            }).collect(Collectors.toList());

//        TODO 发送给es进行保存 mall-search
            R r = searchFeignService.productStatusUp(upProduct);
            if (r.getCode() == 0) {
                //远程调用成功
                //DONETODO 修改spu的状态
                this.baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
            } else {
                //远程调用失败
                //TODO 重复调用，接口幂等性，重试机制
                //Feign的调用流程
                /*
                    1.构造请求数据，将对象转为json
                    2.发送请求进行执行
                    3.执行请求会有重试机制

                 */
            }
        }
    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        SkuInfoEntity skuInfoEntity = skuInfoService.getById(skuId);
        Long spuId = skuInfoEntity.getSpuId();
        return this.getById(spuId);
    }

}