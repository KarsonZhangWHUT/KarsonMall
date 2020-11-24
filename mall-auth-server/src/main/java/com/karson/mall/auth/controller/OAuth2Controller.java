package com.karson.mall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.karson.common.constant.AuthServerConstant;
import com.karson.common.utils.HttpUtils;
import com.karson.common.utils.R;
import com.karson.common.vo.MemberResponseVo;
import com.karson.mall.auth.feign.MemberFeignService;
import com.karson.mall.auth.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Karson
 * 处理社交登录请求
 */
@Controller
@Slf4j
public class OAuth2Controller {

    @Autowired
    private MemberFeignService memberFeignService;

    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {
        //根据code换取accessToken
        Map<String, String> map = new HashMap<>();
        map.put("client_id", "1273051720");
        map.put("client_secret", "fed207085f5c5ed6fd1807f079481ae2");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.mall.com/oauth2.0/weibo/success");
        map.put("code", code);
        Map<String, String> headers = new HashMap<>();
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", headers, null, map);

        //处理
        if (response.getStatusLine().getStatusCode() == 200) {
            //获取accessToken
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);
            //相当于知道了是哪个社交用户
            //如果是第一次进入网站，相当于注册，以后这个社交账号就对应一个商城用户
            //登陆或者注册这个用户
            R r = memberFeignService.oauthLogin(socialUser);
            if (r.getCode() == 0) {
                MemberResponseVo data = r.getData(new TypeReference<MemberResponseVo>() {
                });
                log.info("登陆成功，用户信息：{}", data.toString());

                //登录成功就跳回首页
                session.setAttribute(AuthServerConstant.LOGIN_USER,data);
                return "redirect://mall.com";
            } else {
                return "redirect://auth.mall.com/login.html";
            }
        } else {
            return "redirect://auth.mall.com/login.html";
        }
    }
}
