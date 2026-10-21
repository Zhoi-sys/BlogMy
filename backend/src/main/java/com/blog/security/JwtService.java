package com.blog.security;

import com.blog.config.BlogProperties;
import com.blog.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Access Token 的签发与校验服务。
 *
 * <p>使用 HS256 对称签名，密钥来自 {@code blog.jwt.secret}（Base64，解码后至少
 * 32 字节）。Token 中携带用户 ID、用户名、角色、类型和 jti；Refresh Token
 * 是 Redis 中的随机不透明字符串，不在此服务中签发。</p>
 *
 * @since 1.0.0
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final BlogProperties properties;

    /**
     * 创建 JWT 服务并校验密钥强度。
     *
     * @param properties 应用配置
     * @throws IllegalStateException 密钥为空或解码后不足 32 字节时抛出
     */
    public JwtService(BlogProperties properties) {
        this.properties = properties;
        String secret = properties.jwt().secret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET must be configured");
        }
        byte[] bytes = Decoders.BASE64.decode(secret);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must decode to at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    /**
     * 为用户签发 Access Token。
     *
     * @param user 用户实体
     * @return Token 字符串、jti 和过期时间
     */
    public IssuedAccessToken issueAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.jwt().accessTtl());
        String jti = UUID.randomUUID().toString();
        String token = Jwts.builder()
                .subject(Long.toString(user.getId()))
                .claim("username", user.getUsername())
                .claim("role", user.getRole())
                .claim("type", "access")
                .id(jti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
        return new IssuedAccessToken(token, jti, expiresAt);
    }

    /**
     * 校验并解析 Access Token。
     *
     * @param token Bearer Token 原文
     * @return Token 声明
     * @throws JwtException Token 无效、过期或类型不是 access 时抛出
     */
    public TokenClaims parseAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        if (!"access".equals(claims.get("type", String.class))) {
            throw new JwtException("not an access token");
        }
        Number role = claims.get("role", Number.class);
        return new TokenClaims(
                Long.parseLong(claims.getSubject()),
                claims.get("username", String.class),
                role == null ? 1 : role.intValue(),
                claims.getId(),
                claims.getExpiration().toInstant()
        );
    }

    /**
     * 新签发的 Access Token 信息。
     *
     * @param token     JWT 字符串
     * @param jti       JWT 唯一标识，用于登出黑名单
     * @param expiresAt 过期时间
     */
    public record IssuedAccessToken(String token, String jti, Instant expiresAt) {
    }

    /**
     * 从 Access Token 中解析出的声明。
     *
     * @param userId    用户 ID
     * @param username  用户名
     * @param role      角色
     * @param jti       JWT 唯一标识
     * @param expiresAt 过期时间
     */
    public record TokenClaims(Long userId, String username, int role, String jti, Instant expiresAt) {
    }
}
