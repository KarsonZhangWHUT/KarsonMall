package com.karson.mall.cart.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Karson
 * 整个购物车
 */
//@Data
public class Cart {

    private List<CartItem> items;
    private Integer countNum;//商品数量
    private Integer countType;//商品类型

    private BigDecimal totalAmount;

    private BigDecimal reduce = new BigDecimal(0);//减免价格

    public Integer getCountNum() {
        int i = 0;
        if (items != null && items.size() > 0) {
            for (CartItem item : items) {
                i += item.getCount();
            }
        }
        return i;
    }

    public Integer getCountType() {
        if (items != null)
            return items.size();
        return 0;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal bigDecimal = new BigDecimal(0);
        if (items != null && items.size() > 0) {
            for (CartItem item : items) {
                bigDecimal = bigDecimal.add(item.getTotalPrice());
            }
        }
        bigDecimal = bigDecimal.subtract(reduce);
        return bigDecimal;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
