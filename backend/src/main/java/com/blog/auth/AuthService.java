package com.blog.auth;

import com.blog.auth.dto.AuthDtos;
import com.blog.security.UserPrincipal;
import com.blog.user.dto.UserDtos;

/**
 * 认证领域服务接口。实现见 {@code com.blog.auth.impl.AuthServiceImpl}。
 */
public interface AuthService {

    AuthDtos.TokenResponse register(AuthDtos.RegisterRequest request, String ip);

    AuthDtos.TokenResponse login(AuthDtos.LoginRequest request, String ip);

    AuthDtos.TokenResponse refresh(String refreshToken);

    void logout(UserPrincipal principal, String refreshToken);

    UserDtos.UserView me(UserPrincipal principal);
}
