package com.karson.mall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.karson.common.utils.PageUtils;
import com.karson.common.utils.Query;
import com.karson.mall.ware.dao.WareInfoDao;
import com.karson.mall.ware.entity.WareInfoEntity;
import com.karson.mall.ware.service.WareInfoService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        String key = (String) params.get("key");

        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> {
                w.eq("id", key).or().like("name", key)
                        .or().like("address", key)
                        .or().like("areacode", key);
            });
        }

        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wrapper);

        return new PageUtils(page);
    }

}