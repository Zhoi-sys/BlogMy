package com.blog.auth;

import com.blog.auth.dto.AuthDtos;
import com.blog.security.UserPrincipal;
import com.blog.user.dto.UserDtos;

/**
 * 认证领域服务接口。
 *
 * <p>负责账号注册、登录、令牌签发与轮换、登出和当前用户查询。
 * 具体实现见 {@code com.blog.auth.impl.AuthServiceImpl}。</p>
 *
 * @since 1.0.0
 */
public interface AuthService {

    /**
     * 注册读者账号并签发登录令牌。
     *
     * @param request 注册请求，包含唯一用户名和明文密码
     * @param ip      客户端 IP，用于注册频率限制
     * @return Access Token、Refresh Token、有效期和用户资料
     */
    AuthDtos.TokenResponse register(AuthDtos.RegisterRequest request, String ip);

    /**
     * 使用用户名和密码登录。
     *
     * @param request 登录凭据
     * @param ip      客户端 IP，用于登录频率限制
     * @return 新签发的令牌对与用户资料
     */
    AuthDtos.TokenResponse login(AuthDtos.LoginRequest request, String ip);

    /**
     * 使用一次性 Refresh Token 换取新的令牌对。
     *
     * <p>刷新成功后旧 Refresh Token 立即失效；重复使用会返回未授权。</p>
     *
     * @param refreshToken 刷新令牌
     * @return 新的 Access Token、Refresh Token、有效期和用户资料
     */
    AuthDtos.TokenResponse refresh(String refreshToken);

    /**
     * 登出当前会话。
     *
     * <p>将 Access Token 的 jti 加入黑名单，并删除传入的 Refresh Token。</p>
     *
     * @param principal    当前登录用户，包含 jti 与过期时间
     * @param refreshToken 可选的一次性刷新令牌
     */
    void logout(UserPrincipal principal, String refreshToken);

    /**
     * 查询当前登录用户资料。
     *
     * @param principal 当前登录用户
     * @return 当前用户视图
     */
    UserDtos.UserView me(UserPrincipal principal);
}
