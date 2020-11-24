package com.karson.mall.thirdparty;

import com.aliyun.oss.OSSClient;
import com.karson.mall.thirdparty.component.SMSComponent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest
class MallThirdPartyApplicationTests {

    @Autowired
    private OSSClient ossClient;

    @Autowired
    SMSComponent smsComponent;


    @Test
    public void testUpload2() throws FileNotFoundException {
        ossClient.putObject("karson-mall", "hhh-compress.jpg", new FileInputStream("E:\\images\\L181kk2Eou-compress.jpg"));
    }

    @Test
    void contextLoads() {
        System.out.println(smsComponent);
        smsComponent.sendSMSCode("17764037126","123456");
    }

}
