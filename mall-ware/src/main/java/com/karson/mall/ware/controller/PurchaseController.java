package com.karson.mall.ware.controller;

import com.karson.common.utils.PageUtils;
import com.karson.common.utils.R;
import com.karson.mall.ware.entity.PurchaseEntity;
import com.karson.mall.ware.service.PurchaseService;
import com.karson.mall.ware.vo.MergeVo;
import com.karson.mall.ware.vo.PurchaseDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 采购信息
 *
 * @author karson
 * @email 308780714@qq.com
 * @date 2020-11-07 16:23:45
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;



    /**
     * 完成采购单
     */
    @PostMapping("/done")
    public R done(@RequestBody PurchaseDoneVo purchaseDoneVo) {
        //TODO 判断purchaseDoneVo再数据库中有记录的,有记录（采购单）的话才能采购商品
        purchaseService.done(purchaseDoneVo);
        return R.ok();
    }


    /**
     * 领取采购单
     */
    @PostMapping("/receive")
    public R receive(@RequestBody List<Long> ids) {
        purchaseService.received(ids);

        return R.ok();
    }


    @RequestMapping("/unreceive/list")
    public R unReceivedList(@RequestParam Map<String, Object> params) {
        PageUtils page = purchaseService.unReceived(params);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:purchase:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }

    @PostMapping("/merge")
    public R merge(@RequestBody MergeVo mergeVo) {

        System.out.println(mergeVo);
        purchaseService.mergePurchase(mergeVo);
        return R.ok();
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:purchase:info")
    public R info(@PathVariable("id") Long id) {
        PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase) {
        purchase.setCreateTime(new Date());
        purchase.setUpdateTime(new Date());
        purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase) {
        purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:purchase:delete")
    public R delete(@RequestBody Long[] ids) {
        purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
