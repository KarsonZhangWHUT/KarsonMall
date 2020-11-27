package com.karson.mall.order.listener;

import com.karson.common.to.mq.SecKillOrderTo;
import com.karson.mall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Karson
 */
@Component
@RabbitListener(queues = "order.seckill.order.queue")
@Slf4j
public class OrderSeckillListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void listener(SecKillOrderTo secKillOrder, Channel channel, Message message) throws IOException {
        try {
            log.info("准备创建秒杀单的详细信息");

            orderService.createSeckillOrder(secKillOrder);

            channel.basicAck(message.getMessageProperties().getDeliveryTag(),
                    false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),
                    true);
        }

    }

}
