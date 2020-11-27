package com.karson.mall.ware.listener;

import com.karson.common.to.mq.OrderTo;
import com.karson.common.to.mq.StockLockedTo;
import com.karson.mall.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author Karson
 */
@Service
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {


    @Autowired
    private WareSkuService wareSkuService;

    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo stockLockedTo,
                                         Message message,
                                         Channel channel) throws IOException {
        System.out.println("收到解锁的库存的消息");
        try {
            wareSkuService.unLockStock(stockLockedTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    public void handleOrderCloseRelease(OrderTo orderTo, Message message,
                                        Channel channel) throws IOException {
        System.out.println("订单关闭消息，准备解锁库存");
        try {
            wareSkuService.unLockStock(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);

        }
    }
}
