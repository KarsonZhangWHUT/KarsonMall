package com.karson.seckill.controller;

import com.karson.common.utils.R;
import com.karson.seckill.service.SecKillService;
import com.karson.seckill.to.SecKillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author Karson
 */
@Controller
public class SecKillController {

    @Autowired
    private SecKillService secKillService;

    /**
     * 返回当前时间可以参与的秒杀商品信息
     */
    @ResponseBody
    @GetMapping("/getCurrentTimeSkus")
    public R getCurrentTimeSkus() {
        List<SecKillSkuRedisTo> vos = secKillService.getCurrentTimeSkus();


        return R.ok().setData(vos);
    }

    @ResponseBody
    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSecKillInfo(@PathVariable("skuId") Long skuId) {
        SecKillSkuRedisTo secKillSkuRedisTo = secKillService.getSkuSecKillInfo(skuId);
        return R.ok().setData(secKillSkuRedisTo);
    }

    @GetMapping("/kill")
    public String secKill(@RequestParam("killId") String killId,
                          @RequestParam("key") String key,
                          @RequestParam("num") Integer num,
                          Model model) {
        String orderSn = secKillService.kill(killId, key, num);
        model.addAttribute("orderSn", orderSn);
        return "success";
    }
}
