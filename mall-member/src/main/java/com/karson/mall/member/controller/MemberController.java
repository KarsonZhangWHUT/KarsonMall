package com.karson.mall.member.controller;

import com.karson.common.exception.BizCodeEnum;
import com.karson.common.utils.PageUtils;
import com.karson.common.utils.R;
import com.karson.mall.member.entity.MemberEntity;
import com.karson.mall.member.exception.PhoneExistException;
import com.karson.mall.member.exception.UsernameExistException;
import com.karson.mall.member.feign.CouponFeignService;
import com.karson.mall.member.service.MemberService;
import com.karson.mall.member.vo.MemberLoginVo;
import com.karson.mall.member.vo.MemberRegisterVo;
import com.karson.mall.member.vo.SocialUser;
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
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;


    /**
     * 社交登录
     */
    @PostMapping("/oauth2/login")
    public R oauthLogin(@RequestBody SocialUser socialUser) {
        MemberEntity memberEntity = memberService.login(socialUser);
        if (memberEntity != null)
            //登录成功
            return R.ok().setData(memberEntity);
        else
            return R.error(BizCodeEnum.ACCOUNT_PASSWORD_INVALID_EXCEPTION.getCode(), BizCodeEnum.ACCOUNT_PASSWORD_INVALID_EXCEPTION.getMessage());
    }


    @RequestMapping("/coupons")
    public R test() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");
        R memberCoupons = couponFeignService.memberCoupons();
        return R.ok().put("member", memberEntity).put("coupons", memberCoupons.get("coupns"));
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public R register(@RequestBody MemberRegisterVo vo) {
        try {
            memberService.register(vo);
        } catch (PhoneExistException e) {
            return R.error(BizCodeEnum.PHONE_EXIST_EXCePTION.getCode(), BizCodeEnum.PHONE_EXIST_EXCePTION.getMessage());
        } catch (UsernameExistException e) {
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(), BizCodeEnum.USER_EXIST_EXCEPTION.getMessage());
        }
        return R.ok();
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo) {
        MemberEntity memberEntity = memberService.login(vo);
        if (memberEntity != null)
            return R.ok().setData(memberEntity);
        else
            return R.error(BizCodeEnum.ACCOUNT_PASSWORD_INVALID_EXCEPTION.getCode(), BizCodeEnum.ACCOUNT_PASSWORD_INVALID_EXCEPTION.getMessage());
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
