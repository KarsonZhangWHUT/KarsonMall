package com.karson.mall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.karson.common.constant.AuthServerConstant;
import com.karson.common.exception.BizCodeEnum;
import com.karson.common.utils.R;
import com.karson.common.vo.MemberResponseVo;
import com.karson.mall.auth.feign.MemberFeignService;
import com.karson.mall.auth.feign.ThirdPartyFeignService;
import com.karson.mall.auth.vo.UserLoginVo;
import com.karson.mall.auth.vo.UserRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Karson
 */
@Controller
public class LoginController {

    @Autowired
    private ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MemberFeignService memberFeignService;


    @GetMapping("/login.html")
    public String loginPage(HttpSession session) {
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute == null)
            return "login";
        else
            return "redirect:http://mall.com";
    }


    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone) {
        //接口防刷

        //验证码校验,redis，存K sms:code:18888888888  V 456789
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (null != redisCode && redisCode.length() > 0) {
            long parseLong = Long.parseLong(redisCode.split("_")[1]);
            //防止同一个phone在60s内再次发送验证码
            if (System.currentTimeMillis() - parseLong < 60000L) {
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMessage());
            }
        }
        String code = UUID.randomUUID().toString().substring(0, 5);
        String redisValue = UUID.randomUUID().toString().substring(0, 5) + "_" + System.currentTimeMillis();
        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, redisValue, 10, TimeUnit.MINUTES);
//        thirdPartyFeignService.sendCode(phone,code); //短信验证码服务到期
        return R.ok();
    }

    @PostMapping("/register")
    public String register(@Valid UserRegisterVo vo, BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            List<FieldError> fieldErrors = result.getFieldErrors();
            Map<String, String> errors = fieldErrors.stream().collect(Collectors.toMap(FieldError::getField, fieldError -> fieldError.getDefaultMessage()));
            redirectAttributes.addFlashAttribute("errors", errors);
            //校验出错，转发到注册页
            return "redirect:http://auth.mall.com/reg.html";
        }
        //校验验证码
        String code = vo.getCode();
        String redisValue = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (!StringUtils.isEmpty(redisValue)) {
            if (code.equals(redisValue.split("_")[0])) {
                //删除验证码
                stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
                //验证码正确，真正注册，调用远程服务进行注册
                R r = memberFeignService.register(vo);
                if (r.getCode() == 0) {
                    //调用成功
                    return "redirect:http://auth.mall.com/login.html";

                } else {
                    //调用失败
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", r.getData("msg", new TypeReference<String>() {
                    }));
                    redirectAttributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.mall.com/reg.html";
                }
            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "验证码错误");
                redirectAttributes.addFlashAttribute("errors", errors);
                //校验出错，转发到注册页
                return "redirect:http://auth.mall.com/reg.html";
            }
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", errors);
            //校验出错，转发到注册页
            return "redirect:http://auth.mall.com/reg.html";
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes,
                        HttpSession session) {
        //调用远程member服务，验证登录信息

        R login = memberFeignService.login(vo);
        if (login.getCode() == 0) {
            MemberResponseVo data = login.getData(new TypeReference<MemberResponseVo>() {
            });
            //登陆成功，放到session中
            session.setAttribute(AuthServerConstant.LOGIN_USER, data);
            return "redirect:http://mall.com";
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", login.getData("msg", new TypeReference<String>() {
            }));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.mall.com/login.html";
        }

    }
}
