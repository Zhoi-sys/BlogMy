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

@Service
public class JwtService {

    private final SecretKey key;
    private final BlogProperties properties;

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

    public record IssuedAccessToken(String token, String jti, Instant expiresAt) {
    }

    public record TokenClaims(Long userId, String username, int role, String jti, Instant expiresAt) {
    }
}
