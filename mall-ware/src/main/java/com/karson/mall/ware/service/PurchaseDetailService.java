package com.karson.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.karson.common.utils.PageUtils;
import com.karson.mall.ware.entity.PurchaseDetailEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author karson
 * @email 308780714@qq.com
 * @date 2020-11-07 16:23:45
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<PurchaseDetailEntity> listDetailByPurchaseId(Long id);
}

