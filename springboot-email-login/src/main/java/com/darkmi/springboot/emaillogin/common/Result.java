package com.darkmi.springboot.emaillogin.common;

/**
 * 统一返回结果（极简版）：code = 0 表示成功，非 0 表示业务失败。
 */
public record Result<T>(int code, String msg, T data) {

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(0, msg, data);
    }

    public static Result<Void> success(String msg) {
        return new Result<>(0, msg, null);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(1, msg, null);
    }
}
