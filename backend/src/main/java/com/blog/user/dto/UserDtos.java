package com.blog.user.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class UserDtos {

    private UserDtos() {
    }

    public record UserView(
            Long id,
            String username,
            String nickname,
            String avatarUrl,
            String bio,
            int role,
            LocalDateTime createdAt
    ) {
    }

    public record UserSummary(
            Long id,
            String username,
            String nickname,
            String avatarUrl,
            int role
    ) {
    }

    public record UserStats(
            long articleCount,
            long likeCount,
            long viewCount,
            long commentCount
    ) {
    }

    public record UserProfile(
            UserView user,
            UserStats stats
    ) {
    }

    public record UpdateProfileRequest(
            @Size(min = 2, max = 32, message = "昵称长度需为 2-32 个字符")
            String nickname,
            @Size(max = 255, message = "简介不能超过 255 个字符")
            String bio
    ) {
    }
}
