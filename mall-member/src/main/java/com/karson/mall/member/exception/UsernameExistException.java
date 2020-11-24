package com.karson.mall.member.exception;

/**
 * @author Karson
 */
public class UsernameExistException extends RuntimeException {
    public UsernameExistException() {
        super("用户名已存在");
    }
}
