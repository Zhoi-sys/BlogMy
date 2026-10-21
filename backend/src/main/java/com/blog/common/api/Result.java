package com.blog.common.api;

/**
 * 统一 REST 响应包装。
 *
 * <p>HTTP 成功响应仍使用 200，业务结果由 {@code code} 表达：
 * {@code 0} 表示成功，非 0 表示错误。参数错误、未登录等场景同时返回对应的
 * HTTP 状态码。</p>
 *
 * @param code 业务状态码，0 成功
 * @param msg  面向调用方的提示信息
 * @param data 业务数据，可能为 {@code null}
 * @param <T>  业务数据类型
 * @since 1.0.0
 */
public record Result<T>(int code, String msg, T data) {

    /**
     * 构造带数据的成功响应。
     *
     * @param data 业务数据
     * @param <T>  业务数据类型
     * @return 成功响应
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(0, "OK", data);
    }

    /**
     * 构造无数据的成功响应。
     *
     * @return 成功响应
     */
    public static Result<Void> ok() {
        return new Result<>(0, "OK", null);
    }

    /**
     * 构造错误响应。
     *
     * @param code 业务错误码
     * @param msg  错误提示
     * @return 错误响应
     */
    public static Result<Void> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }
}
