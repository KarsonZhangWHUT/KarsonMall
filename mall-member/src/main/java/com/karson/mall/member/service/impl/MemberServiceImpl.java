package com.karson.mall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.karson.common.utils.HttpUtils;
import com.karson.common.utils.PageUtils;
import com.karson.common.utils.Query;
import com.karson.mall.member.dao.MemberDao;
import com.karson.mall.member.dao.MemberLevelDao;
import com.karson.mall.member.entity.MemberEntity;
import com.karson.mall.member.entity.MemberLevelEntity;
import com.karson.mall.member.exception.PhoneExistException;
import com.karson.mall.member.exception.UsernameExistException;
import com.karson.mall.member.service.MemberService;
import com.karson.mall.member.vo.MemberLoginVo;
import com.karson.mall.member.vo.MemberRegisterVo;
import com.karson.mall.member.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Resource
    private MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void register(MemberRegisterVo vo) {
        MemberDao memberDao = this.baseMapper;
        MemberEntity memberEntity = new MemberEntity();

        //检查用户名和手机号是否唯一,让controller能感知异常，使用异常机制
        checkPhoneUnique(vo.getPhone());
        checkUsernameUnique(vo.getUserName());

        //设置默认等级
        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(levelEntity.getId());

        memberEntity.setUsername(vo.getUserName());
        memberEntity.setMobile(vo.getPhone());
        memberEntity.setNickname(vo.getUserName());

        //密码要进行加密存储
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        memberEntity.setPassword(encode);


        memberDao.insert(memberEntity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        MemberDao memberDao = this.baseMapper;
        Integer mobile = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (mobile > 0)
            throw new PhoneExistException();
    }

    @Override
    public void checkUsernameUnique(String username) throws UsernameExistException {
        MemberDao memberDao = this.baseMapper;
        Integer count = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (count > 0)
            throw new UsernameExistException();
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginAccount = vo.getLoginAccount();
        String password = vo.getPassword();
        //去数据库查询
        MemberDao memberDao = this.baseMapper;
        MemberEntity memberEntity = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginAccount).or().eq("mobile", loginAccount));
        if (memberEntity == null) {
            //登陆失败
            return null;
        } else {
            String passwordFromDB = memberEntity.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            //进行密码匹配
            boolean matches = passwordEncoder.matches(password, passwordFromDB);
            if (matches) {
                return memberEntity;
            } else
                return null;
        }
    }

    @Override
    public MemberEntity login(SocialUser socialUser) {
        //具有登录和注册合并逻辑
        String uid = socialUser.getUid();
        //判断当前社交用户是否已经登陆过系统
        MemberDao memberDao = this.baseMapper;
        MemberEntity memberEntity = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if (memberEntity != null) {
            //这个用户已经注册了，重新更换一下令牌和过期时间
            MemberEntity update = new MemberEntity();
            update.setId(memberEntity.getId());
            update.setAccessToken(socialUser.getAccessToken());
            update.setExpireIn(socialUser.getExpiresIn());
            memberDao.updateById(update);

            memberEntity.setAccessToken(socialUser.getAccessToken());
            memberEntity.setExpireIn(socialUser.getExpiresIn());

            return memberEntity;
        } else {
            //没有查到当前社交用户的记录，需要注册一个
            MemberEntity register = new MemberEntity();
            //查出当前社交用户的账号信息
            // 2. 没有查到当前社交用户对应的记录 我们就需要注册一个
            HashMap<String, String> map = new HashMap<>();
            map.put("access_token", socialUser.getAccessToken());
            map.put("uid", socialUser.getUid());
            try {
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<>(), map);
                // 3. 查询当前社交用户账号信息(昵称、性别等)
                if(response.getStatusLine().getStatusCode() == 200){
                    // 查询成功
                    String json = EntityUtils.toString(response.getEntity());
                    // 这个JSON对象什么样的数据都可以直接获取
                    JSONObject jsonObject = JSON.parseObject(json);
                    register.setNickname(jsonObject.getString("name"));
                    register.setUsername(jsonObject.getString("name"));
                    register.setGender("m".equals(jsonObject.getString("gender"))?1:0);
                    register.setCity(jsonObject.getString("location"));
                    register.setJob("自媒体");
                    register.setEmail(jsonObject.getString("email"));
                }
            } catch (Exception e) {
                log.warn("社交登录时远程调用出错 [尝试修复]");
            }
            register.setStatus(0);
            register.setCreateTime(new Date());
            register.setBirth(new Date());
            register.setLevelId(1L);
            register.setSocialUid(socialUser.getUid());
            register.setAccessToken(socialUser.getAccessToken());
            register.setExpireIn(socialUser.getExpiresIn());

            // 注册 -- 登录成功
            memberDao.insert(register);
            register.setPassword(null);
            return register;
        }

    }

}