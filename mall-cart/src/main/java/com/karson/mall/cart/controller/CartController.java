package com.karson.mall.cart.controller;

import com.karson.mall.cart.service.CartService;
import com.karson.mall.cart.vo.Cart;
import com.karson.mall.cart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Karson
 */
@Controller
public class CartController {

    @Autowired
    private CartService cartService;


    @ResponseBody
    @GetMapping("/currentCartItems")
    public List<CartItem> getCurrentCartItems(){
        return cartService.getUserCartItems();
    }


    /**
     * 去购物车页面
     * 浏览器有以个cookie,user-key;标识用户身份，一个月后过期
     * 如果第一次使用，都会给一个临时的用户身份,即给浏览器一个cookie
     * 浏览器以后每次访问都会带上这个cookie
     * <p>
     * 登录，session里有响应的数据
     * 没登录，创建一个cookie，user-key
     * 第一次，如果没有临时用户，创建一个临时用户，即给浏览器放一个cookie
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {

        Cart cart = cartService.getCart();

        model.addAttribute("cart", cart);
        return "cartList";
    }

    /**
     * 添加商品到购物车
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num,
                            Model model,
                            RedirectAttributes attributes) throws ExecutionException, InterruptedException {

        CartItem cartItem = cartService.addToCart(skuId, num);

        attributes.addAttribute("skuId", skuId);
        return "redirect:http://cart.mall.com/addToCartSuccessPage.html";
    }

    /**
     * 跳转到成功页
     */
    @GetMapping("addToCartSuccessPage.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId,
                                       Model model) {
        //重定向到页面，再次查询购物者即可
        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("item", cartItem);
        return "success";
    }


    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("check") Integer check) {
        cartService.checkItem(skuId, check);
        return "redirect:http://cart.mall.com/cart.html";
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num) {

        cartService.countItem(skuId, num);

        return "redirect:http://cart.mall.com/cart.html";
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteItem(skuId);
        return "redirect:http://cart.mall.com/cart.html";
    }
}
