package com.darkmi.springboot.jwtlogin.common;

/**
 * 统一返回结果（极简版）：code = 0 表示成功，非 0 表示业务失败。
 * 认证失败时 code = 401，与 HTTP 状态码保持一致，方便前端统一处理。
 */
public record Result<T>(int code, String msg, T data) {

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(0, msg, data);
    }

    public static Result<Void> success(String msg) {
        return new Result<>(0, msg, null);
    }

    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }
}
