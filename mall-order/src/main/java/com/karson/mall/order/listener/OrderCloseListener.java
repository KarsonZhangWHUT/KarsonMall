package com.karson.mall.order.listener;

import com.karson.mall.order.entity.OrderEntity;
import com.karson.mall.order.service.OrderService;
import com.rabbitmq.client.Channel;
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
@RabbitListener(queues = "order.release.order.queue")
public class OrderCloseListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void listener(OrderEntity entity, Channel channel, Message message) throws IOException {
        System.out.println("关单" + entity.getOrderSn() + "\n" + entity.getModifyTime());

        try {
            orderService.closeOrder(entity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),
                    false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),
                    true);
        }


    }

}
