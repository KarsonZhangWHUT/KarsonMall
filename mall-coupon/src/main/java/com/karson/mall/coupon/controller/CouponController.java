package com.karson.mall.coupon.controller;

import com.karson.common.utils.PageUtils;
import com.karson.common.utils.R;
import com.karson.mall.coupon.entity.CouponEntity;
import com.karson.mall.coupon.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;


/**
 * 优惠券信息
 *
 * @author karson
 * @email 308780714@qq.com
 * @date 2020-11-07 15:53:17
 */
@RefreshScope
@RestController
@RequestMapping("coupon/coupon" )
public class CouponController {
    @Autowired
    private CouponService couponService;

    @Value("${coupon.user.name}" )
    private String name;
    @Value("${coupon.user.age}" )
    private Integer age;

    @RequestMapping("/test" )
    public R test() {
        return R.ok().put("name", name).put("age", age);
    }


    @RequestMapping("/member/list" )
    public R memberCoupons() {

        CouponEntity couponEntity = new CouponEntity();
        couponEntity.setCouponName("满100减10" );
        return R.ok().put("coupons", Collections.singletonList(couponEntity));
    }

    /**
     * 列表
     */
    @RequestMapping("/list" )
    //@RequiresPermissions("coupon:coupon:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = couponService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}" )
    //@RequiresPermissions("coupon:coupon:info")
    public R info(@PathVariable("id" ) Long id) {
        CouponEntity coupon = couponService.getById(id);

        return R.ok().put("coupon", coupon);
    }

    /**
     * 保存
     */
    @RequestMapping("/save" )
    //@RequiresPermissions("coupon:coupon:save")
    public R save(@RequestBody CouponEntity coupon) {
        couponService.save(coupon);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update" )
    //@RequiresPermissions("coupon:coupon:update")
    public R update(@RequestBody CouponEntity coupon) {
        couponService.updateById(coupon);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete" )
    //@RequiresPermissions("coupon:coupon:delete")
    public R delete(@RequestBody Long[] ids) {
        couponService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
