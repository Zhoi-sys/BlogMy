package com.blog.security;

import com.blog.common.store.KeyValueStore;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final KeyValueStore store;

    public JwtAuthenticationFilter(JwtService jwtService, KeyValueStore store) {
        this.jwtService = jwtService;
        this.store = store;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7).strip();
            try {
                JwtService.TokenClaims claims = jwtService.parseAccessToken(token);
                boolean blacklisted = store.exists("auth:blacklist:" + claims.jti());
                if (!blacklisted) {
                    UserPrincipal principal = new UserPrincipal(
                            claims.userId(), claims.username(), claims.role(), claims.jti(), claims.expiresAt());
                    var authorities = List.of(new SimpleGrantedAuthority(
                            principal.isAdmin() ? "ROLE_ADMIN" : "ROLE_USER"));
                    var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JwtException | IllegalArgumentException ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
