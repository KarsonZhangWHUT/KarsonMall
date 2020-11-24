package com.karson.mall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.karson.common.utils.R;
import com.karson.mall.cart.feign.ProductFeignService;
import com.karson.mall.cart.interceptor.CartInterceptor;
import com.karson.mall.cart.service.CartService;
import com.karson.mall.cart.to.UserInfoTo;
import com.karson.mall.cart.vo.Cart;
import com.karson.mall.cart.vo.CartItem;
import com.karson.mall.cart.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


/**
 * @author Karson
 */
@Service
@Slf4j
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor executor;


    private final String CART_PREFIX = "mall:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        //根据userKey或者userId+skuId判断购物车中有没有这件商品，如果有，则数量+num，否则是添加新的额商品
        String str = (String) cartOps.get(skuId.toString());
        if (StringUtils.isEmpty(str)) {
            CartItem cartItem = new CartItem();
//            当购物车中没有这件商品
            //向购物车中添加新商品
            //远程查询当前要添加的商品信息
            CompletableFuture<Void> getSkuInfoFuture = CompletableFuture.runAsync(() -> {
                R skuInfo = productFeignService.getSkuInfo(skuId);
                SkuInfoVo data = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                //把商品添加到购物车，即放到缓存中
                cartItem.setChecked(false);
                cartItem.setCount(num);
                cartItem.setImage(data.getSkuDefaultImg());
                cartItem.setPrice(data.getPrice());
                cartItem.setSkuId(skuId);
                cartItem.setSkuTitle(data.getSkuTitle());
            }, executor);

            //远程查询sku的组合信息，线程池，两个远程调用异步执行
            CompletableFuture<Void> getSaleAttrFuture = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(skuSaleAttrValues);

            }, executor);

            CompletableFuture.allOf(getSaleAttrFuture, getSkuInfoFuture).get();
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        } else {
            //购物车中有这个商品
            CartItem cartItem = JSON.parseObject(str, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String str = (String) cartOps.get(skuId.toString());
        return JSON.parseObject(str, CartItem.class);
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        Cart cart = new Cart();
        //区分用户等没登陆
        UserInfoTo userInfo = CartInterceptor.threadLocal.get();
        if (userInfo.getUserId() != null) {
            //登陆状态,获取登录了的数据并且合并离线状态下添加的购物项
            String cartKey = CART_PREFIX + userInfo.getUserId();
            BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(cartKey);
            //如果离线购物车中有数据，合并购物车
            String tempCartKey = CART_PREFIX + userInfo.getUserKey();
            List<CartItem> tempCartItems = getCartItems(tempCartKey);
            if (tempCartItems != null && tempCartItems.size() > 0) {
                //临时购物车有数据，将临时购物车的商品添加到购物车
                for (CartItem tempCartItem : tempCartItems) {
                    addToCart(tempCartItem.getSkuId(), tempCartItem.getCount());
                }
                //清空购物车的数据
                clearCart(tempCartKey);

            }
            //获取登陆后的购物车的数据
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);

        } else {
            //没登陆
            String userKey = userInfo.getUserKey();
            String cartKey = CART_PREFIX + userKey;
            //获取临时购车的购物项
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }
        return cart;
    }

    /**
     * 获取到要操作的购物车
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfo = CartInterceptor.threadLocal.get();
        //1.判断用户登录与否，来选择用哪个购物车
        String cartKey = "";
        if (userInfo.getUserId() != null) {
            cartKey = CART_PREFIX + userInfo.getUserId();
        } else {
            cartKey = CART_PREFIX + userInfo.getUserKey();
        }
        return stringRedisTemplate.boundHashOps(cartKey);
    }

    private List<CartItem> getCartItems(String cartKey) {

        BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOps.values();
        if (values != null && values.size() > 0) {
            return values.stream().map(value -> {

                String str = (String) value;
                return JSON.parseObject(str, CartItem.class);
            }).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public void clearCart(String cartKey) {
        stringRedisTemplate.delete(cartKey);
    }

    /**
     * 勾选购物项
     */
    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setChecked(check == 1);
        String string = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),string);
    }

    @Override
    public void countItem(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        String string = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),string);
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }
}
