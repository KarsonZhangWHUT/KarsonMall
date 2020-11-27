package com.karson.mall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.karson.common.utils.PageUtils;
import com.karson.common.utils.Query;
import com.karson.mall.coupon.dao.SeckillSessionDao;
import com.karson.mall.coupon.entity.SeckillSessionEntity;
import com.karson.mall.coupon.entity.SeckillSkuRelationEntity;
import com.karson.mall.coupon.service.SeckillSessionService;
import com.karson.mall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    private SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLatest3DaysSession() {
        //计算最近三天
//        Data data = new Data();
        List<SeckillSessionEntity> list = this.list(new QueryWrapper<SeckillSessionEntity>()
                .between("start_time", startTime(), endTime()));
        if (list != null && list.size() > 0) {
            return list.stream().peek(session -> {
                Long sessionId = session.getId();
                List<SeckillSkuRelationEntity> skuRelationEntities = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>()
                        .eq("promotion_session_id", sessionId));
                session.setRelationSkus(skuRelationEntities);
            }).collect(Collectors.toList());
        }
        return null;
    }

    private String startTime() {
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        LocalDateTime start = LocalDateTime.of(now, min);
        return start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String endTime() {
        LocalDate now = LocalDate.now();
        LocalDate plus2 = now.plusDays(2);
        LocalDateTime end = LocalDateTime.of(plus2, LocalTime.MAX);
        return end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}