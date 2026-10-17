package com.blog.security;

import java.security.Principal;
import java.time.Instant;

public record UserPrincipal(Long id, String username, int role, String jti, Instant expiresAt) implements Principal {

    public boolean isAdmin() {
        return role == 9;
    }

    @Override
    public String getName() {
        return username;
    }
}
