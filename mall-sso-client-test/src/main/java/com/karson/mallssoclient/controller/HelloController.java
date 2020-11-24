package com.karson.mallssoclient.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Karson
 */
@Controller
public class HelloController {


    @Value("${sso.server.url}")
    private String ssoServerURL;


    /**
     * 不需要登陆就可以访问
     */
    @GetMapping("/hello")
    @ResponseBody
    public String hello() {
        return "hello";
    }


    @GetMapping("/employees")
    public String employees(Model model, HttpSession session) {

        Object loginUser = session.getAttribute("loginUser");
        if (loginUser == null) {
            //没登陆,跳转到登陆服务器登录
            return "redirect:"+ssoServerURL;
        } else {
            List<String> emps = new ArrayList<>();
            emps.add("张三");
            emps.add("李四");
            model.addAttribute("emps", emps);
            return "list";
        }
    }


}
