package com.karson.mall.member.exception;

/**
 * @author Karson
 */
public class PhoneExistException extends RuntimeException {
    public PhoneExistException() {
        super("手机号已注册");
    }
}
