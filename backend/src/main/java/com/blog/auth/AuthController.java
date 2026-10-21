package com.blog.auth;

import com.blog.auth.dto.AuthDtos;
import com.blog.common.api.Result;
import com.blog.common.util.WebUtils;
import com.blog.security.UserPrincipal;
import com.blog.user.dto.UserDtos;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口。
 *
 * <p>提供注册、登录、刷新令牌、登出和当前用户查询。登录成功返回短期 Access Token
 * 与一次性 Refresh Token；除注册、登录、刷新外均需要携带
 * {@code Authorization: Bearer <accessToken>}。</p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * 创建认证控制器。
     *
     * @param authService 认证服务
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 注册读者账号并直接签发登录令牌。
     *
     * @param request        用户名与密码，用户名需唯一
     * @param servletRequest 用于提取客户端 IP 做注册限流
     * @return Access Token、Refresh Token、过期时间与用户信息
     */
    @PostMapping("/register")
    public Result<AuthDtos.TokenResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest request,
                                                   HttpServletRequest servletRequest) {
        return Result.ok(authService.register(request, WebUtils.clientIp(servletRequest)));
    }

    /**
     * 用户名密码登录。
     *
     * @param request        登录凭据
     * @param servletRequest 用于提取客户端 IP 做登录限流
     * @return 登录令牌与用户信息
     */
    @PostMapping("/login")
    public Result<AuthDtos.TokenResponse> login(@Valid @RequestBody AuthDtos.LoginRequest request,
                                                HttpServletRequest servletRequest) {
        return Result.ok(authService.login(request, WebUtils.clientIp(servletRequest)));
    }

    /**
     * 使用 Refresh Token 换取新的令牌对。
     *
     * <p>Refresh Token 采用一次性轮换策略，旧令牌在刷新成功后立即失效。</p>
     *
     * @param request 包含 refreshToken
     * @return 新的令牌对与用户信息
     */
    @PostMapping("/refresh")
    public Result<AuthDtos.TokenResponse> refresh(@Valid @RequestBody AuthDtos.RefreshRequest request) {
        return Result.ok(authService.refresh(request.refreshToken()));
    }

    /**
     * 登出当前会话。
     *
     * <p>服务端将当前 Access Token 的 jti 写入黑名单，直至其自然过期；
     * 若同时传入 Refresh Token，也会一并删除。</p>
     *
     * @param principal 当前登录用户，由安全过滤器注入
     * @param request   可选的 Refresh Token 请求体
     * @return 空成功响应
     */
    @PostMapping("/logout")
    public Result<Void> logout(@AuthenticationPrincipal UserPrincipal principal,
                               @RequestBody(required = false) AuthDtos.RefreshRequest request) {
        authService.logout(principal, request == null ? null : request.refreshToken());
        return Result.ok();
    }

    /**
     * 查询当前登录用户信息。
     *
     * @param principal 当前登录用户
     * @return 当前用户资料
     */
    @GetMapping("/me")
    public Result<UserDtos.UserView> me(@AuthenticationPrincipal UserPrincipal principal) {
        return Result.ok(authService.me(principal));
    }
}
