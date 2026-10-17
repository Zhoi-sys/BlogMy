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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Result<AuthDtos.TokenResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest request,
                                                   HttpServletRequest servletRequest) {
        return Result.ok(authService.register(request, WebUtils.clientIp(servletRequest)));
    }

    @PostMapping("/login")
    public Result<AuthDtos.TokenResponse> login(@Valid @RequestBody AuthDtos.LoginRequest request,
                                                HttpServletRequest servletRequest) {
        return Result.ok(authService.login(request, WebUtils.clientIp(servletRequest)));
    }

    @PostMapping("/refresh")
    public Result<AuthDtos.TokenResponse> refresh(@Valid @RequestBody AuthDtos.RefreshRequest request) {
        return Result.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@AuthenticationPrincipal UserPrincipal principal,
                               @RequestBody(required = false) AuthDtos.RefreshRequest request) {
        authService.logout(principal, request == null ? null : request.refreshToken());
        return Result.ok();
    }

    @GetMapping("/me")
    public Result<UserDtos.UserView> me(@AuthenticationPrincipal UserPrincipal principal) {
        return Result.ok(authService.me(principal));
    }
}
