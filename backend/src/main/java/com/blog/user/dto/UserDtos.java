package com.blog.user.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * 用户与个人空间相关 DTO 集合。
 *
 * @since 1.0.0
 */
public final class UserDtos {

    private UserDtos() {
    }

    /**
     * 用户完整视图。
     *
     * @param id        用户 ID
     * @param username  登录用户名
     * @param nickname  展示昵称
     * @param avatarUrl 头像地址，一期为空时前端使用文字头像
     * @param bio       个人简介
     * @param role      角色：1 读者，9 博主/管理员
     * @param createdAt 注册时间
     */
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

    /**
     * 用户摘要，用于文章作者、评论作者和 @ 联想结果。
     *
     * @param id        用户 ID
     * @param username  登录用户名
     * @param nickname  展示昵称
     * @param avatarUrl 头像地址
     * @param role      角色：1 读者，9 博主/管理员
     */
    public record UserSummary(
            Long id,
            String username,
            String nickname,
            String avatarUrl,
            int role
    ) {
    }

    /**
     * 用户空间公开统计。
     *
     * @param articleCount 已发布文章数
     * @param likeCount    已发布文章累计获赞数
     * @param viewCount    已发布文章累计阅读数
     * @param commentCount 已发布文章累计评论数
     */
    public record UserStats(
            long articleCount,
            long likeCount,
            long viewCount,
            long commentCount
    ) {
    }

    /**
     * 用户空间头部聚合数据。
     *
     * @param user  用户资料
     * @param stats 公开统计
     */
    public record UserProfile(
            UserView user,
            UserStats stats
    ) {
    }

    /**
     * 修改个人资料请求；字段为 {@code null} 表示保持原值。
     *
     * @param nickname 新昵称，2-32 个字符
     * @param bio      新简介，最多 255 个字符
     */
    public record UpdateProfileRequest(
            @Size(min = 2, max = 32, message = "昵称长度需为 2-32 个字符")
            String nickname,
            @Size(max = 255, message = "简介不能超过 255 个字符")
            String bio
    ) {
    }
}
