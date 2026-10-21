package com.blog.common.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Web 请求工具。
 *
 * @since 1.0.0
 */
public final class WebUtils {

    private WebUtils() {
    }

    /**
     * 提取客户端 IP。
     *
     * <p>优先读取反向代理设置的 {@code X-Forwarded-For} 第一段；否则使用
     * Servlet 的远端地址。该值用于阅读去重与限流，部署时应确保代理覆盖该头。</p>
     *
     * @param request HTTP 请求
     * @return 客户端 IP
     */
    public static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].strip();
        }
        return request.getRemoteAddr();
    }
}
