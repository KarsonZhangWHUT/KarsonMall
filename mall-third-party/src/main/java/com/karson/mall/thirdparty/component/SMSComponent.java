package com.karson.mall.thirdparty.component;

import com.karson.mall.thirdparty.util.HttpUtils;
import lombok.Data;
import org.apache.http.HttpResponse;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Karson
 */
@Component
@Data
@ConfigurationProperties(prefix = "spring.cloud.alicloud.sms")
public class SMSComponent {
    /*
    "https://cdcxdxjk.market.alicloudapi.com";
            String path = "/chuangxin/dxjk";
     */
    private String host;
    private String path;
    private String content;  //"【创信】验证码:"
    private String appcode;  //"e15b71db8d9049e2820e2d13e9e8e2ce";//开通服务后 买家中心-查看AppCode

    public void sendSMSCode(String phone, String code) {
        String method = "POST";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("content", content+code);
        querys.put("mobile", phone);
        Map<String, String> bodys = new HashMap<String, String>();

        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
