package com.karson.mall.coupon.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.karson.common.utils.PageUtils;
import com.karson.common.utils.Query;

import com.karson.mall.coupon.dao.SeckillPromotionDao;
import com.karson.mall.coupon.entity.SeckillPromotionEntity;
import com.karson.mall.coupon.service.SeckillPromotionService;


@Service("seckillPromotionService")
public class SeckillPromotionServiceImpl extends ServiceImpl<SeckillPromotionDao, SeckillPromotionEntity> implements SeckillPromotionService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillPromotionEntity> page = this.page(
                new Query<SeckillPromotionEntity>().getPage(params),
                new QueryWrapper<SeckillPromotionEntity>()
        );

        return new PageUtils(page);
    }

}