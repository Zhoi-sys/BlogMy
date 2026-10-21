package com.blog.common.exception;

/**
 * 业务异常。
 *
 * <p>异常同时携带 HTTP 状态码与业务错误码 {@code code}，由
 * {@link GlobalExceptionHandler} 统一转换为 {@code Result} 响应。
 * 业务代码应通过静态工厂方法创建异常，避免状态码与错误码不一致。</p>
 *
 * @since 1.0.0
 */
public class BusinessException extends RuntimeException {

    /** HTTP 状态码。 */
    private final int status;

    /** 业务错误码。 */
    private final int code;

    /**
     * 创建业务异常。
     *
     * @param status  HTTP 状态码
     * @param code    业务错误码
     * @param message 面向调用方的错误信息
     */
    public BusinessException(int status, int code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    /**
     * 创建参数错误异常（HTTP 400，业务码 40001）。
     *
     * @param message 错误信息
     * @return 业务异常
     */
    public static BusinessException badRequest(String message) {
        return new BusinessException(400, 40001, message);
    }

    /**
     * 创建未认证异常（HTTP 401，业务码 40100）。
     *
     * @param message 错误信息
     * @return 业务异常
     */
    public static BusinessException unauthorized(String message) {
        return new BusinessException(401, 40100, message);
    }

    /**
     * 创建无权限异常（HTTP 403，业务码 40300）。
     *
     * @param message 错误信息
     * @return 业务异常
     */
    public static BusinessException forbidden(String message) {
        return new BusinessException(403, 40300, message);
    }

    /**
     * 创建资源不存在异常（HTTP 404，业务码 40400）。
     *
     * @param message 错误信息
     * @return 业务异常
     */
    public static BusinessException notFound(String message) {
        return new BusinessException(404, 40400, message);
    }

    /**
     * 创建状态冲突异常（HTTP 409，业务码 40900）。
     *
     * @param message 错误信息
     * @return 业务异常
     */
    public static BusinessException conflict(String message) {
        return new BusinessException(409, 40900, message);
    }

    /**
     * 创建请求过频异常（HTTP 429，业务码 42900）。
     *
     * @param message 错误信息
     * @return 业务异常
     */
    public static BusinessException tooManyRequests(String message) {
        return new BusinessException(429, 42900, message);
    }

    /**
     * 获取 HTTP 状态码。
     *
     * @return HTTP 状态码
     */
    public int getStatus() {
        return status;
    }

    /**
     * 获取业务错误码。
     *
     * @return 业务错误码
     */
    public int getCode() {
        return code;
    }
}
