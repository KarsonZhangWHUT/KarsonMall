package com.karson.mall.auth.vo;

import lombok.Data;
import lombok.ToString;

/**
 * @author Karson
 */
@Data
@ToString
public class UserLoginVo {
    private String loginAccount;
    private String password;
}
