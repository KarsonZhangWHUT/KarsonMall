package com.karson.mall.order;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class MallOrderApplicationTests {

    /**
     * 1 创建Exchange Queue Binding
     * 使用AmqpAdmin及逆行创建
     * 2 收发消息
     */

    @Autowired
    AmqpAdmin amqpAdmin;



    /**
     * 2 收发消息
     */

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    void sendMessage(){
        rabbitTemplate.convertAndSend("hello-java-exchange",
                "hello-java",
                "HelloWorld");
    }







    @Test
    void contextLoads() {
        DirectExchange directExchange = new DirectExchange("hello-java-exchange",
                true,
                false);
        amqpAdmin.declareExchange(directExchange);
        log.info("exchange{}创建成功", "hello-java-exchange");

    }

    @Test
    void createQueue() {
        Queue queue = new Queue("hello-java-queue",
                true,
                false,
                false);
        amqpAdmin.declareQueue(queue);
        log.info("队列{}", "hello-java-queue");
    }

    @Test
    void createBinding() {
        //String destination, DestinationType destinationType, String exchange, String routingKey,
        //			@Nullable Map<String, Object> arguments
        Binding binding = new Binding("hello-java-queue",
                Binding.DestinationType.QUEUE,
                "hello-java-exchange",
                "hello-java",
                null);
        amqpAdmin.declareBinding(binding);
        log.info("Binding{}", "hello-java-queue");

    }


}
