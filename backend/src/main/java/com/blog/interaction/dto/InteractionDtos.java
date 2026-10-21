package com.blog.interaction.dto;

import com.blog.user.dto.UserDtos;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 点赞、评论、@ 回复与通知相关 DTO 集合。
 *
 * @since 1.0.0
 */
public final class InteractionDtos {

    private InteractionDtos() {
    }

    /**
     * 点赞/取消点赞请求。
     *
     * @param targetType 目标类型：1 文章、2 评论
     * @param targetId   目标 ID
     */
    public record LikeRequest(
            @NotNull(message = "缺少点赞对象类型")
            @Min(value = 1, message = "点赞对象类型不正确")
            @Max(value = 2, message = "点赞对象类型不正确")
            Integer targetType,
            @NotNull(message = "缺少点赞对象")
            Long targetId
    ) {
    }

    /**
     * 点赞操作结果。
     *
     * @param liked     操作后是否处于已点赞状态
     * @param likeCount 操作后的最新点赞数
     */
    public record LikeResult(boolean liked, long likeCount) {
    }

    /**
     * 发表评论/回复请求。
     *
     * @param articleId      文章 ID
     * @param content        评论正文，最多 1000 字
     * @param parentId       被回复的评论 ID；一级评论为空或 0
     * @param replyToUserId  被回复用户 ID；服务端会根据 parentId 自动校正
     * @param mentionUserIds 显式 @ 的用户 ID，最多 10 个
     */
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

    /**
     * 评论视图。
     *
     * @param id             评论 ID
     * @param articleId      文章 ID
     * @param parentId       父评论 ID；一级评论为 0
     * @param author         评论作者；已删除评论为 {@code null}
     * @param contentHtml    清洗后的评论 HTML，保留 @ 链接与换行
     * @param deleted        是否已软删除
     * @param likeCount      点赞数
     * @param liked          当前用户是否已点赞
     * @param replyToUserId  被回复用户 ID
     * @param replyToNickname 被回复用户昵称
     * @param replyCount     一级评论下的回复总数；回复本身为 0
     * @param previewReplies 回复预览列表；仅一级评论填充
     * @param createdAt      发表时间
     */
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

    /**
     * 标记通知已读请求；{@code all=true} 时忽略 ids。
     *
     * @param ids 要标记的通知 ID 列表，最多 100 条
     * @param all 是否全部标记已读
     */
    public record ReadRequest(
            List<Long> ids,
            Boolean all
    ) {
    }

    /**
     * 未读通知数量。
     *
     * @param count 未读数量
     */
    public record UnreadCount(long count) {
    }

    /**
     * 通知视图。
     *
     * @param id             通知 ID
     * @param type           类型：1 回复、2 提及、3 评论了我的文章
     * @param actor          触发通知的用户
     * @param articleId      关联文章 ID
     * @param articleTitle   关联文章标题，文章删除后可能为空
     * @param commentId      关联评论 ID
     * @param contentPreview 评论内容摘要
     * @param read           是否已读
     * @param createdAt      通知时间
     */
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
