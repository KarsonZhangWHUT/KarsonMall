package com.karson.mall.member.controller;

import com.karson.common.utils.PageUtils;
import com.karson.common.utils.R;
import com.karson.mall.member.entity.MemberEntity;
import com.karson.mall.member.feign.CouponFeignService;
import com.karson.mall.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 会员
 *
 * @author karson
 * @email 308780714@qq.com
 * @date 2020-11-07 16:08:57
 */
@RestController
@RequestMapping("member/member" )
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;

    @RequestMapping("/coupons" )
    public R test() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三" );
        R memberCoupons = couponFeignService.memberCoupons();
        return R.ok().put("member", memberEntity).put("coupons", memberCoupons.get("coupns" ));
    }


    /**
     * 列表
     */
    @RequestMapping("/list" )
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}" )
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id" ) Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save" )
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update" )
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete" )
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
