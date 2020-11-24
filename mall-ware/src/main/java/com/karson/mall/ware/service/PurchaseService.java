package com.karson.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.karson.common.utils.PageUtils;
import com.karson.mall.ware.entity.PurchaseEntity;
import com.karson.mall.ware.vo.MergeVo;
import com.karson.mall.ware.vo.PurchaseDoneVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author karson
 * @email 308780714@qq.com
 * @date 2020-11-07 16:23:45
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils unReceived(Map<String, Object> params);

    void mergePurchase(MergeVo mergeVo);

    void received(List<Long> ids);

    void done(PurchaseDoneVo purchaseDoneVo);
}

