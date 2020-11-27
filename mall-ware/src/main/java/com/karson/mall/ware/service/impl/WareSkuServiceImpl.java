package com.karson.mall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.karson.common.exception.NoStockException;
import com.karson.common.to.mq.OrderTo;
import com.karson.common.to.mq.StockDetailTo;
import com.karson.common.to.mq.StockLockedTo;
import com.karson.common.utils.PageUtils;
import com.karson.common.utils.Query;
import com.karson.common.utils.R;
import com.karson.mall.ware.dao.WareSkuDao;
import com.karson.mall.ware.entity.WareOrderTaskDetailEntity;
import com.karson.mall.ware.entity.WareOrderTaskEntity;
import com.karson.mall.ware.entity.WareSkuEntity;
import com.karson.mall.ware.feign.OrderFeignService;
import com.karson.mall.ware.feign.ProductFeignService;
import com.karson.mall.ware.service.WareOrderTaskDetailService;
import com.karson.mall.ware.service.WareOrderTaskService;
import com.karson.mall.ware.service.WareSkuService;
import com.karson.mall.ware.vo.OrderItemVo;
import com.karson.mall.ware.vo.OrderVo;
import com.karson.mall.ware.vo.SkuHasStockVo;
import com.karson.mall.ware.vo.WareSkuLockVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
@Slf4j
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Resource
    private WareSkuDao wareSkuDao;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private WareOrderTaskService wareOrderTaskService;

    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    private OrderFeignService orderFeignService;

    /**
     * 库存自动解锁
     */


    private void unLockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {
        wareSkuDao.unLockStock(skuId, wareId, num);
        //更新库存工作单的状态
        WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity();
        wareOrderTaskDetailEntity.setId(taskDetailId);
        wareOrderTaskDetailEntity.setLockStatus(2);//已解锁
        wareOrderTaskDetailService.updateById(wareOrderTaskDetailEntity);
    }


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /*
                skuId: 1
                wareId: 1
         */
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");

        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {

        //判断如果没有这个库存记录，就是新增操作，否则是更新操作
        List<WareSkuEntity> wareSkuEntities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (wareSkuEntities == null || wareSkuEntities.size() == 0) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            //DONETODO 远程查询sku的名字 如果失败整个事务无需回滚
            try {
                R info = productFeignService.info(skuId);
                if (info.getCode() == 0) {
                    Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
                    wareSkuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {
            }
            wareSkuDao.insert(wareSkuEntity);
        } else
            wareSkuDao.addStock(skuId, wareId, skuNum);
    }

    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds) {
        return skuIds.stream().map(skuId -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            //查询当前sku的总库存量
            Long count = this.baseMapper.getSkuStock(skuId);
            skuHasStockVo.setSkuId(skuId);
            skuHasStockVo.setHasStock(count != null && count > 0);
            return skuHasStockVo;
        }).collect(Collectors.toList());
    }

    /**
     * 为订单锁定库存
     */
    @Override
    @Transactional
    public Boolean orderLockStock(WareSkuLockVo vo) {

        /*
            保存库存工作单的详情
            追溯
         */
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        log.info(wareOrderTaskEntity.toString());
        wareOrderTaskService.save(wareOrderTaskEntity);
        log.info(wareOrderTaskEntity.toString());


        //找到商品都在哪个仓库都库存
        List<OrderItemVo> itemNeedLocked = vo.getItemNeedLocked();
        List<SkuWareHasStock> collect = itemNeedLocked.stream().map(item -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            skuWareHasStock.setSkuId(skuId);
            skuWareHasStock.setNum(item.getCount());
            //查询在哪有库存
            List<Long> wareIds = wareSkuDao.listWareIdHasStock(skuId);
            skuWareHasStock.setWareId(wareIds);
            return skuWareHasStock;
        }).collect(Collectors.toList());

        //锁定库存
        for (SkuWareHasStock skuWareHasStock : collect) {
            boolean skuStocked = false;
            Long skuId = skuWareHasStock.getSkuId();
            List<Long> wareIds = skuWareHasStock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                throw new NoStockException(skuId.toString());
            }
            //如果每一个商品都锁定成功，将当前商品锁定了几件的工作单记录发给MQ
            //锁定失败。前面保存的工作单信息就回滚，发送出去的消息，即使要解锁记录，由于在数据库查不到记录，，不影响功能

            for (Long wareId : wareIds) {
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, skuWareHasStock.getNum());
                if (count == 1) {
                    skuStocked = true;
                    //库存锁定成功，告诉MQ库存锁定成功
                    WareOrderTaskDetailEntity wareOrderTaskDetailEntity
                            = new WareOrderTaskDetailEntity(null,
                            skuId,
                            "",
                            skuWareHasStock.getNum(),
                            wareOrderTaskEntity.getId(),
                            wareId,
                            1);
                    wareOrderTaskDetailService.save(wareOrderTaskDetailEntity);
                    StockLockedTo stockLocked = new StockLockedTo();
                    stockLocked.setId(wareOrderTaskEntity.getId());
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(wareOrderTaskDetailEntity, stockDetailTo);
                    stockLocked.setStockDetailTo(stockDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange",
                            "stock.locked",
                            stockLocked);
                    break;
                }
                //当前仓库锁失败，重试下一个仓库
            }
            if (!skuStocked) {
                throw new NoStockException(skuId.toString());
            }

        }
        //全部锁定成功
        return true;
    }

    @Override
    public void unLockStock(StockLockedTo stockLockedTo) {
        StockDetailTo stockDetailTo = stockLockedTo.getStockDetailTo();
        Long stockDetailToId = stockDetailTo.getId();
/*        解锁，查询数据库关于这个订单的锁定库存信息
        有：证明库存锁定成功了
              解锁：分情况
                    1.没有订单，回滚库存
                    2.有这个订单
                        2.1 订单状态，已取消，解锁库存
                        2.2 订单没取消 不能解锁库存
        没有：库存锁定失败了，库存回滚了，这种情况无需解锁
        */
        WareOrderTaskDetailEntity byId = wareOrderTaskDetailService.getById(stockDetailToId);
        if (byId != null) {
            //解锁
            Long id = stockLockedTo.getId();
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn();
            R orderStatus = orderFeignService.getOrderStatus(orderSn);
            if (orderStatus.getCode() == 0) {
                //远程查询成功
                OrderVo data = orderStatus.getData(new TypeReference<OrderVo>() {
                });
                if (data == null || data.getStatus() == 4) {
                    //说明订单已经被取消了，解锁库存
                    if (byId.getLockStatus() == 1)
                        unLockStock(stockDetailTo.getSkuId(),
                                stockDetailTo.getWareId(),
                                stockDetailTo.getSkuNum(),
                                stockDetailToId);
                }
            } else {
                throw new RuntimeException("远程服务失败");
            }

        }

    }

    //防止订单服务卡顿，导致订单消息一致改不了，库存消息优先到期，
    //查订单状态为新建状态，什么都不做
    //导致卡顿的订单，永远不能解锁
    @Override
    @Transactional
    public void unLockStock(OrderTo orderTo) {
        //查一下库存解锁的状态，防止重复解锁
       WareOrderTaskEntity orderTaskEntity=  wareOrderTaskService.getOrderTaskByOrderSn(orderTo.getOrderSn());
        Long orderTaskId = orderTaskEntity.getId();
        //按照工作单 没有解锁的库存，进行解锁
        List<WareOrderTaskDetailEntity> entities = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>()
                .eq("task_id", orderTaskId)
                .eq("lock_status", 1));

        for (WareOrderTaskDetailEntity entity : entities) {
            unLockStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum(),entity.getTaskId());
        }

    }


    @Data
    static
    class SkuWareHasStock {

        private Long skuId;

        private List<Long> wareId;

        private Integer num;
    }


}