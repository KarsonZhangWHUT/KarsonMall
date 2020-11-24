package com.karson.mallssoserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author Karson
 */
@Controller
public class LoginController {


    @GetMapping("/login.html")
    public String loginPage(){
        return "login";
    }

    @PostMapping("/doLogin")
    public String doLogin() {

        //之前是从哪跳到这的，就跳回到哪里
        return "";
    }
}
