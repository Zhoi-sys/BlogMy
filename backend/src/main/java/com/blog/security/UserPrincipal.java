package com.blog.security;

import java.security.Principal;
import java.time.Instant;

/**
 * 已验证的当前用户主体。
 *
 * <p>由 {@link JwtAuthenticationFilter} 从 Access Token 中解析并放入
 * Spring Security 上下文。保留 jti 与过期时间，便于登出时精确加入黑名单。</p>
 *
 * @param id        用户 ID
 * @param username  用户名
 * @param role      角色：1 读者，9 博主/管理员
 * @param jti       JWT 唯一标识
 * @param expiresAt Access Token 过期时间
 * @since 1.0.0
 */
public record UserPrincipal(Long id, String username, int role, String jti, Instant expiresAt) implements Principal {

    /**
     * 判断是否为博主/管理员。
     *
     * @return 角色为 9 时返回 {@code true}
     */
    public boolean isAdmin() {
        return role == 9;
    }

    /**
     * 返回 Spring Security 使用的用户名。
     *
     * @return 用户名
     */
    @Override
    public String getName() {
        return username;
    }
}
