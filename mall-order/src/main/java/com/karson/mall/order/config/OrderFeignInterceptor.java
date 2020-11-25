package com.karson.mall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Karson
 */
@Controller
public class OrderFeignInterceptor {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {

        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                //feign远程调用之前先进行RequestInterceptor.apply
                //把请求头放到Feign建立的请求中

                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    //同步请求头信息，最主要，同步cookie
                    requestTemplate.header("Cookie", request.getHeader("Cookie"));
                }
            }
        };
    }
}
