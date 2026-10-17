package com.blog.auth.impl;

import com.blog.auth.AuthService;
import com.blog.auth.dto.AuthDtos;
import com.blog.common.exception.BusinessException;
import com.blog.common.store.KeyValueStore;
import com.blog.common.store.RateLimiter;
import com.blog.config.BlogProperties;
import com.blog.security.JwtService;
import com.blog.security.UserPrincipal;
import com.blog.user.User;
import com.blog.user.UserMapper;
import com.blog.user.UserService;
import com.blog.user.UserViewMapper;
import com.blog.user.dto.UserDtos;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;

@Service
public class AuthServiceImpl implements AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserMapper userMapper;
    private final UserService userService;
    private final UserViewMapper userViewMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final KeyValueStore store;
    private final BlogProperties properties;
    private final RateLimiter rateLimiter;

    public AuthServiceImpl(UserMapper userMapper, UserService userService, UserViewMapper userViewMapper,
                           PasswordEncoder passwordEncoder, JwtService jwtService, KeyValueStore store,
                           BlogProperties properties, RateLimiter rateLimiter) {
        this.userMapper = userMapper;
        this.userService = userService;
        this.userViewMapper = userViewMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.store = store;
        this.properties = properties;
        this.rateLimiter = rateLimiter;
    }

    @Override
    @Transactional
    public AuthDtos.TokenResponse register(AuthDtos.RegisterRequest request, String ip) {
        rateLimiter.check("register-ip", ip, 5, Duration.ofHours(1));
        String username = normalizeUsername(request.username());
        if (userService.findByUsername(username) != null) {
            throw BusinessException.conflict("用户名已被占用");
        }
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setNickname(username);
        user.setAvatarUrl("");
        user.setBio("");
        user.setRole(1);
        user.setStatus(1);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException ex) {
            throw BusinessException.conflict("用户名已被占用");
        }
        return issueTokens(user);
    }

    @Override
    public AuthDtos.TokenResponse login(AuthDtos.LoginRequest request, String ip) {
        rateLimiter.check("login-ip", ip, 10, Duration.ofMinutes(1));
        String username = normalizeUsername(request.username());
        rateLimiter.check("login-user", username, 10, Duration.ofMinutes(1));
        User user = userService.findByUsername(username);
        if (user == null || user.getStatus() == null || user.getStatus() != 1
                || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw BusinessException.unauthorized("用户名或密码错误");
        }
        return issueTokens(user);
    }

    @Override
    public AuthDtos.TokenResponse refresh(String refreshToken) {
        String key = refreshKey(refreshToken);
        String userIdValue = store.get(key);
        if (userIdValue == null) {
            throw BusinessException.unauthorized("登录状态已过期，请重新登录");
        }
        // Refresh token 一次性使用，刷新后立即作废旧令牌，降低泄露后的重放风险。
        store.delete(key);
        User user = userService.requireActive(Long.parseLong(userIdValue));
        return issueTokens(user);
    }

    @Override
    public void logout(UserPrincipal principal, String refreshToken) {
        if (principal != null) {
            long seconds = Duration.between(Instant.now(), principal.expiresAt()).toSeconds();
            if (seconds > 0) {
                store.set("auth:blacklist:" + principal.jti(), "1", Duration.ofSeconds(seconds));
            }
        }
        if (refreshToken != null && !refreshToken.isBlank()) {
            store.delete(refreshKey(refreshToken));
        }
    }

    @Override
    public UserDtos.UserView me(UserPrincipal principal) {
        return userViewMapper.toView(userService.requireActive(principal.id()));
    }

    /**
     * Access Token + 一次性 Refresh Token 的统一签发入口。
     */
    private AuthDtos.TokenResponse issueTokens(User user) {
        JwtService.IssuedAccessToken access = jwtService.issueAccessToken(user);
        String refreshToken = randomToken();
        Duration refreshTtl = properties.jwt().refreshTtl();
        store.set(refreshKey(refreshToken), Long.toString(user.getId()), refreshTtl);
        return new AuthDtos.TokenResponse(
                access.token(),
                refreshToken,
                properties.jwt().accessTtl().toSeconds(),
                userViewMapper.toView(user)
        );
    }

    private String randomToken() {
        byte[] bytes = new byte[36];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String refreshKey(String token) {
        return "auth:refresh:" + token;
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.strip().toLowerCase(Locale.ROOT);
    }
}
