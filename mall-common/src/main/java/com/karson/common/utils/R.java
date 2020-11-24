/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package com.karson.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回数据
 * 不能带泛型，R<T>是错的，只能向R里里面put数据
 *
 * @author Mark sunlightcs@gmail.com
 */
public class R extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    //    T data;

    public R setData(Object data) {
        put("data", data);
        return this;
    }

    /**
     * 获取指定key的value
     */
    public <T> T getData(String key, TypeReference<T> typeReference) {
        Object data = get(key);
        return JSON.parseObject(JSON.toJSONString(data), typeReference);
    }


    /**
     * 获取指定key“data”的value
     */
    public <T> T getData(TypeReference<T> typeReference) {
        Object data = get("data");
        return JSON.parseObject(JSON.toJSONString(data), typeReference);
    }


    public R() {
        put("code", 0);
        put("msg", "success");
    }

    public static R error() {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
    }

    public static R error(String msg) {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
    }

    public static R error(int code, String msg) {
        R r = new R();
        r.put("code", code);
        r.put("msg", msg);
        return r;
    }

    public static R ok(String msg) {
        R r = new R();
        r.put("msg", msg);
        return r;
    }

    public static R ok(Map<String, Object> map) {
        R r = new R();
        r.putAll(map);
        return r;
    }

    public static R ok() {
        return new R();
    }

    public R put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    public int getCode() {

        return (int) this.get("code");
    }
}
