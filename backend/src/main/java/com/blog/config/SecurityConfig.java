package com.blog.config;

import com.blog.common.api.Result;
import com.blog.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 配置。
 *
 * <p>采用无状态 JWT 鉴权：关闭 Session、CSRF、表单登录和 HTTP Basic，
 * 在用户名密码过滤器之前加入 {@link JwtAuthenticationFilter}。公开读取接口
 * 放行，写操作按角色或登录状态保护。</p>
 *
 * @since 1.0.0
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * 密码编码器。
     *
     * @return BCrypt（cost=10）编码器
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * 构建安全过滤器链与接口授权规则。
     *
     * @param http         HttpSecurity 构建器
     * @param jwtFilter    JWT 认证过滤器
     * @param objectMapper JSON 序列化器，用于输出 401/403 统一响应
     * @return 安全过滤器链
     * @throws Exception 构建失败时抛出
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter,
                                            ObjectMapper objectMapper) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login", "/api/auth/refresh").permitAll()
                        .requestMatchers("/api/auth/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/site", "/api/feed").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/search").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/users/*/articles").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/articles/manage").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/articles/**").permitAll()
                        .requestMatchers("/api/articles/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/comments/**").permitAll()
                        .requestMatchers("/api/comments/**").authenticated()
                        .requestMatchers("/api/likes/**").authenticated()
                        .requestMatchers("/api/notifications/**").authenticated()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                writeError(response, objectMapper, 401, 40100, "请先登录"))
                        .accessDeniedHandler((request, response, deniedException) ->
                                writeError(response, objectMapper, 403, 40300, "没有权限执行此操作")))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * 输出与 {@link Result} 一致的 401/403 JSON。
     *
     * @param response     HTTP 响应
     * @param objectMapper JSON 序列化器
     * @param status       HTTP 状态码
     * @param code         业务错误码
     * @param message      错误信息
     * @throws IOException 写出失败时抛出
     */
    private void writeError(HttpServletResponse response, ObjectMapper objectMapper,
                            int status, int code, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), Result.error(code, message));
    }

    /**
     * 构建 CORS 配置。
     *
     * <p>允许来源由 {@code blog.cors.allowed-origins} 配置，多个用逗号分隔；
     * 认证通过 Authorization 头传递，因此不开启 Cookie 凭据。</p>
     *
     * @param properties 应用配置
     * @return CORS 配置源
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource(BlogProperties properties) {
        CorsConfiguration configuration = new CorsConfiguration();
        String origins = properties.cors() == null ? "" : properties.cors().allowedOrigins();
        configuration.setAllowedOrigins(Arrays.stream(origins.split(","))
                .map(String::strip)
                .filter(value -> !value.isBlank())
                .toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Content-Disposition"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
