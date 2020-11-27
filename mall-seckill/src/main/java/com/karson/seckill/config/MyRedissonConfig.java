package com.karson.seckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Karson
 */
@Configuration
public class MyRedissonConfig {

    /**
     * 所有随Reidsson的操作都是通过RedissonClient对象
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        //创建配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://118.31.38.90:6379");
        //根据Config创建RedissonClient的实例
        return Redisson.create(config);
    }
}
