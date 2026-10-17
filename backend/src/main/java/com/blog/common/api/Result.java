package com.blog.common.api;

public record Result<T>(int code, String msg, T data) {

    public static <T> Result<T> ok(T data) {
        return new Result<>(0, "OK", data);
    }

    public static Result<Void> ok() {
        return new Result<>(0, "OK", null);
    }

    public static Result<Void> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }
}
