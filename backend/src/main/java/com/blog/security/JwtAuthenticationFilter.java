package com.blog.security;

import com.blog.common.store.KeyValueStore;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器。
 *
 * <p>每个请求最多执行一次：读取 Bearer Token，校验签名与过期时间，检查 jti
 * 是否已进入登出黑名单；校验通过后把 {@link UserPrincipal} 放入安全上下文。
 * Token 缺失或无效时不直接拒绝请求，交由 Spring Security 的授权规则决定
 * 是公开访问还是返回 401。</p>
 *
 * @since 1.0.0
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final KeyValueStore store;

    /**
     * 创建过滤器。
     *
     * @param jwtService JWT 服务
     * @param store      黑名单存储
     */
    public JwtAuthenticationFilter(JwtService jwtService, KeyValueStore store) {
        this.jwtService = jwtService;
        this.store = store;
    }

    /**
     * 解析请求中的 Bearer Token 并建立安全上下文。
     *
     * @param request     HTTP 请求
     * @param response    HTTP 响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 异常
     * @throws IOException      IO 异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7).strip();
            try {
                JwtService.TokenClaims claims = jwtService.parseAccessToken(token);
                boolean blacklisted = store.exists("auth:blacklist:" + claims.jti());
                if (!blacklisted) {
                    UserPrincipal principal = new UserPrincipal(
                            claims.userId(), claims.username(), claims.role(), claims.jti(), claims.expiresAt());
                    var authorities = List.of(new SimpleGrantedAuthority(
                            principal.isAdmin() ? "ROLE_ADMIN" : "ROLE_USER"));
                    var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JwtException | IllegalArgumentException ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
