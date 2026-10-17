package com.blog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "blog")
public record BlogProperties(
        Jwt jwt,
        Admin admin,
        Site site,
        Cors cors,
        RateLimit rateLimit,
        boolean demoData
) {
    public record Jwt(String secret, Duration accessTtl, Duration refreshTtl) {
    }

    public record Admin(String username, String password, String nickname) {
    }

    public record Site(String title, String description, String url) {
    }

    public record Cors(String allowedOrigins) {
    }

    public record RateLimit(boolean enabled) {
    }
}
