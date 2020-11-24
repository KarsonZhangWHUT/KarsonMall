package com.karson.mall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.karson.common.to.MemberPrice;
import com.karson.common.to.SkuReductionTo;
import com.karson.common.utils.PageUtils;
import com.karson.common.utils.Query;
import com.karson.mall.coupon.dao.SkuFullReductionDao;
import com.karson.mall.coupon.entity.MemberPriceEntity;
import com.karson.mall.coupon.entity.SkuFullReductionEntity;
import com.karson.mall.coupon.entity.SkuLadderEntity;
import com.karson.mall.coupon.service.MemberPriceService;
import com.karson.mall.coupon.service.SkuFullReductionService;
import com.karson.mall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    private SkuLadderService skuLadderService;

    @Autowired
    private MemberPriceService memberPriceService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {

        //1.保存满减打折，会员价，sku的优惠信息满减信息mall_sms.sms_sku_ladder\sms_sku_full_reduction\sms_member_price
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuReductionTo.getSkuId());
        skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
        skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
        skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
//        skuLadderEntity.setPrice();  下订单的时候再计算
        if (skuReductionTo.getFullCount() > 0) {
            skuLadderService.save(skuLadderEntity);
        }
        //2.sms_sku_full_reduction
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo, skuFullReductionEntity);
        skuFullReductionEntity.setAddOther(skuReductionTo.getCountStatus());
        if (skuFullReductionEntity.getFullPrice().compareTo(new BigDecimal(0)) > 0) {
            this.save(skuFullReductionEntity);
        }
        //3.sms_member_price
        List<MemberPrice> memberPrice = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> collect = memberPrice.stream().map(price -> {
            MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
            memberPriceEntity.setSkuId(price.getId());
            memberPriceEntity.setMemberLevelId(price.getId());
            memberPriceEntity.setMemberLevelName(price.getName());
            memberPriceEntity.setMemberPrice(price.getPrice());
            memberPriceEntity.setAddOther(1);
            return memberPriceEntity;
        }).filter(price -> {
            return price.getMemberPrice().compareTo(new BigDecimal(0)) > 0;
        }).collect(Collectors.toList());
        memberPriceService.saveBatch(collect);
    }

}