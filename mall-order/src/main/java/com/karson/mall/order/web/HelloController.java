package com.karson.mall.order.web;

import org.springframework.stereotype.Controller;

/**
 * @author Karson
 */
@Controller
public class HelloController {

//    @Autowired
//    RabbitTemplate rabbitTemplate;
//
//    @ResponseBody
//    @GetMapping("/test/createOrder")
//    public String createOrderTest(){
//        OrderEntity orderEntity = new OrderEntity();
//        orderEntity.setOrderSn(UUID.randomUUID().toString());
//        orderEntity.setModifyTime(new Date());
//        //给mq发送消息
//        rabbitTemplate.convertAndSend("order-event-exchange",
//                "order.create.order",orderEntity);
//        return "ok";
//    }
}
