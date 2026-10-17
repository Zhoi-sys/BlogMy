package com.blog.auth.dto;

import com.blog.user.dto.UserDtos;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

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

    public record LoginRequest(
            @NotBlank(message = "请输入用户名")
            @Size(max = 32, message = "用户名过长")
            String username,
            @NotBlank(message = "请输入密码")
            @Size(max = 64, message = "密码过长")
            String password
    ) {
    }

    public record RefreshRequest(
            @NotBlank(message = "刷新令牌不能为空")
            String refreshToken
    ) {
    }

    public record TokenResponse(
            String accessToken,
            String refreshToken,
            long expiresIn,
            UserDtos.UserView user
    ) {
    }
}
