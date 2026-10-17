package com.blog.interaction.dto;

import com.blog.user.dto.UserDtos;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public final class InteractionDtos {

    private InteractionDtos() {
    }

    public record LikeRequest(
            @NotNull(message = "缺少点赞对象类型")
            @Min(value = 1, message = "点赞对象类型不正确")
            @Max(value = 2, message = "点赞对象类型不正确")
            Integer targetType,
            @NotNull(message = "缺少点赞对象")
            Long targetId
    ) {
    }

    public record LikeResult(boolean liked, long likeCount) {
    }

    public record CommentRequest(
            @NotNull(message = "缺少文章 ID")
            Long articleId,
            @NotBlank(message = "评论内容不能为空")
            String content,
            Long parentId,
            Long replyToUserId,
            @Size(max = 10, message = "一次最多提及 10 位用户")
            List<Long> mentionUserIds
    ) {
    }

    public record CommentView(
            Long id,
            Long articleId,
            Long parentId,
            UserDtos.UserSummary author,
            String contentHtml,
            boolean deleted,
            int likeCount,
            boolean liked,
            Long replyToUserId,
            String replyToNickname,
            long replyCount,
            List<CommentView> previewReplies,
            LocalDateTime createdAt
    ) {
    }

    public record ReadRequest(
            List<Long> ids,
            Boolean all
    ) {
    }

    public record UnreadCount(long count) {
    }

    public record NotificationView(
            Long id,
            int type,
            UserDtos.UserSummary actor,
            Long articleId,
            String articleTitle,
            Long commentId,
            String contentPreview,
            boolean read,
            LocalDateTime createdAt
    ) {
    }
}
