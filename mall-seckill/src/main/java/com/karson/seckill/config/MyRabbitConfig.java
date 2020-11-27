package com.karson.seckill.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Karson
 */
@Configuration
public class MyRabbitConfig {

//    @Autowired
//    private RabbitTemplate rabbitTemplate;

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制RabbitTemplate
     */
//    @PostConstruct //MyRabbitConfig对象创建完成以后，执行这个方法
//    public void initRabbitTemplate(){
//        //设置确认回调
//        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
//            @Override
//            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
//                System.out.println("confirm....");
//            }
//        });
//    }

}
