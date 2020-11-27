package com.karson.mall.order.web;

import com.karson.mall.order.service.OrderService;
import com.karson.mall.order.vo.OrderConfirmVo;
import com.karson.mall.order.vo.OrderSubmitVo;
import com.karson.mall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.concurrent.ExecutionException;

/**
 * @author Karson
 */
@Controller
public class OrderWebController {


    @Autowired
    private OrderService orderService;


    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {

        OrderConfirmVo confirmVo = orderService.confirmOrder();

        model.addAttribute("orderConfirmData", confirmVo);
        return "confirm";
    }

    //下单功能
    @PostMapping("/submitorder")
    public String submitOrder(OrderSubmitVo vo, Model model/*, RedirectAttributes redirectAttributes*/) {
        SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);
        //创建订单，验证令牌，验证价格，锁库存
        //下单成功来支付选择页，下单失败回到订单重新确认订单信息
        if (responseVo.getCode() == 0) {
            //成功，去支付页
            model.addAttribute("submitOrderResponseVo", responseVo);
            return "pay";
        } else {
            //失败，跳回toTrade页
//            String msg = "下单失败";
//            switch (responseVo.getCode()) {
//                case 1:
//                    msg += "订单信息过期,请刷新在提交";
//                    break;
//                case 2:
//                    msg += "订单商品价格发送变化,请确认后再次提交";
//                    break;
//                case 3:
//                    msg += "商品库存不足";
//                    break;
//            }
//            redirectAttributes.addFlashAttribute("msg", msg);
            return "redirect:http://order.mall.com/toTrade";
        }
    }
}
