package com.karson.mall.search;

import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class MallSearchApplicationTests {

    @Resource
    private RestHighLevelClient restHighLevelClient;


    @Test
    void indexData() {
    }

    @Test
    void contextLoads() {
        System.out.println(restHighLevelClient);
    }

}
