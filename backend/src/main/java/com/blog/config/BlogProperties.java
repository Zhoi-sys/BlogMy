package com.blog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 博客应用配置，绑定 {@code blog.*} 前缀。
 *
 * @param jwt       JWT 密钥与令牌有效期
 * @param admin     首次启动时创建的博主账号配置
 * @param site      站点标题、简介与公开地址
 * @param cors      允许跨域访问的前端来源，多个来源用逗号分隔
 * @param rateLimit 接口限流开关
 * @param demoData  是否在空库中写入演示文章
 */
@ConfigurationProperties(prefix = "blog")
public record BlogProperties(
        Jwt jwt,
        Admin admin,
        Site site,
        Cors cors,
        RateLimit rateLimit,
        boolean demoData
) {
    /**
     * JWT 配置。
     *
     * @param secret     Base64 编码的 HMAC 密钥，解码后至少 32 字节
     * @param accessTtl  Access Token 有效期
     * @param refreshTtl Refresh Token 有效期
     */
    public record Jwt(String secret, Duration accessTtl, Duration refreshTtl) {
    }

    /**
     * 博主初始化配置。
     *
     * @param username 博主用户名
     * @param password 博主初始密码；为空时不会自动创建账号
     * @param nickname 博主昵称
     */
    public record Admin(String username, String password, String nickname) {
    }

    /**
     * 站点展示配置。
     *
     * @param title       站点标题
     * @param description 站点简介
     * @param url         站点公开地址，用于 SEO 与分享链接
     */
    public record Site(String title, String description, String url) {
    }

    /**
     * 跨域配置。
     *
     * @param allowedOrigins 允许的来源，逗号分隔
     */
    public record Cors(String allowedOrigins) {
    }

    /**
     * 限流配置。
     *
     * @param enabled 是否启用登录、注册、评论等接口限流
     */
    public record RateLimit(boolean enabled) {
    }
}
