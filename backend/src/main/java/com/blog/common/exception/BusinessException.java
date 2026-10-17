package com.blog.common.exception;

public class BusinessException extends RuntimeException {

    private final int status;
    private final int code;

    public BusinessException(int status, int code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(400, 40001, message);
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException(401, 40100, message);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(403, 40300, message);
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(404, 40400, message);
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(409, 40900, message);
    }

    public static BusinessException tooManyRequests(String message) {
        return new BusinessException(429, 42900, message);
    }

    public int getStatus() {
        return status;
    }

    public int getCode() {
        return code;
    }
}
