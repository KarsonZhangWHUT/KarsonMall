package com.karson.mall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.karson.common.utils.PageUtils;
import com.karson.common.utils.Query;
import com.karson.mall.ware.dao.PurchaseDetailDao;
import com.karson.mall.ware.entity.PurchaseDetailEntity;
import com.karson.mall.ware.service.PurchaseDetailService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseDetailEntity> wrapper = new QueryWrapper<>();
        /*
            key:
            status: 0
            wareId: 1
         */

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> {
                w.eq("purchase_id", key).or().like("sku_id", key);
            });
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);

        }
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("status", status);

        }
        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    @Override
    public List<PurchaseDetailEntity> listDetailByPurchaseId(Long id) {
        return this.list(new QueryWrapper<PurchaseDetailEntity>().eq("purchase_id", id));
    }

}