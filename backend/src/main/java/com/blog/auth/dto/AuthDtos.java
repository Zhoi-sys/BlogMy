package com.blog.auth.dto;

import com.blog.user.dto.UserDtos;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 认证接口的请求与响应 DTO 集合。
 *
 * <p>所有字段均通过 Bean Validation 校验，错误信息直接面向接口调用方。</p>
 *
 * @since 1.0.0
 */
public final class AuthDtos {

    private AuthDtos() {
    }

    /**
     * 注册请求。
     *
     * @param username 用户名，3-32 位字母、数字或下划线，注册后不可修改
     * @param password 明文密码，8-64 位且必须同时包含字母和数字
     */
    public record RegisterRequest(
            @NotBlank(message = "请输入用户名")
            @Pattern(regexp = "^[A-Za-z0-9_]{3,32}$", message = "用户名需为 3-32 位字母、数字或下划线")
            String username,
            @NotBlank(message = "请输入密码")
            @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$",
                    message = "密码需为 8-64 位，且同时包含字母和数字")
            String password
    ) {
    }

    /**
     * 登录请求。
     *
     * @param username 用户名
     * @param password 明文密码
     */
    public record LoginRequest(
            @NotBlank(message = "请输入用户名")
            @Size(max = 32, message = "用户名过长")
            String username,
            @NotBlank(message = "请输入密码")
            @Size(max = 64, message = "密码过长")
            String password
    ) {
    }

    /**
     * 刷新令牌请求。
     *
     * @param refreshToken 登录时下发的一次性 Refresh Token
     */
    public record RefreshRequest(
            @NotBlank(message = "刷新令牌不能为空")
            String refreshToken
    ) {
    }

    /**
     * 登录/刷新成功响应。
     *
     * @param accessToken  Access Token，放入 Authorization Bearer 头
     * @param refreshToken 一次性 Refresh Token，刷新成功后轮换
     * @param expiresIn    Access Token 有效期，单位秒
     * @param user         当前用户资料
     */
    public record TokenResponse(
            String accessToken,
            String refreshToken,
            long expiresIn,
            UserDtos.UserView user
    ) {
    }
}
