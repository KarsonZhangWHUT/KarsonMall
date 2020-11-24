package com.karson.mall.cart.to;

import lombok.Data;

/**
 * @author Karson
 */
@Data
public class UserInfoTo {
    private Long userId;
    private String userKey;
    private boolean tempUser = false;
}
