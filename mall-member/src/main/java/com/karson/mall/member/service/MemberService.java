package com.karson.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.karson.common.utils.PageUtils;
import com.karson.mall.member.entity.MemberEntity;
import com.karson.mall.member.exception.PhoneExistException;
import com.karson.mall.member.exception.UsernameExistException;
import com.karson.mall.member.vo.MemberLoginVo;
import com.karson.mall.member.vo.MemberRegisterVo;
import com.karson.mall.member.vo.SocialUser;

import java.util.Map;

/**
 * 会员
 *
 * @author karson
 * @email 308780714@qq.com
 * @date 2020-11-07 16:08:57
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberRegisterVo vo);

    void checkPhoneUnique(String phone) throws PhoneExistException;

    void checkUsernameUnique(String username) throws UsernameExistException;

    MemberEntity login(MemberLoginVo vo);

    MemberEntity login(SocialUser socialUser);
}

