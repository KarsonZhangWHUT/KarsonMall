package com.karson.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.karson.common.to.mq.OrderTo;
import com.karson.common.to.mq.StockLockedTo;
import com.karson.common.utils.PageUtils;
import com.karson.mall.ware.entity.WareSkuEntity;
import com.karson.mall.ware.vo.SkuHasStockVo;
import com.karson.mall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author karson
 * @email 308780714@qq.com
 * @date 2020-11-07 16:23:45
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds);

    Boolean orderLockStock(WareSkuLockVo vo);

    void unLockStock(StockLockedTo stockLockedTo);

    void unLockStock(OrderTo orderTo);
}

