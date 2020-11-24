package com.karson.mall.thirdparty.controller;

import com.karson.common.utils.R;
import com.karson.mall.thirdparty.component.SMSComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Karson
 */
@Controller
@RequestMapping("/sms")
public class SMSController {

    @Autowired
    private SMSComponent smsComponent;

    /**
     * 提供给别的服务进行调用
     */
    @GetMapping("/sendcode")
    public R sendCode(@RequestParam("phone") String phone,@RequestParam("code") String code){
        System.out.println("SMSController：进来了");
        smsComponent.sendSMSCode(phone,code);
        return R.ok();
    }

}
