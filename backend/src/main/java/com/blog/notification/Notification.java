package com.blog.notification;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 站内通知实体，对应 {@code notifications} 表。
 *
 * <p>类型 1 为回复、2 为 @ 提及、3 为文章被评论；{@code is_read} 在 Java 侧
 * 映射为 {@code readFlag}。</p>
 *
 * @since 1.0.0
 */
@Getter
@Setter
public class Notification {

    /** 通知主键。 */
    private Long id;

    /** 通知接收者用户 ID。 */
    private Long userId;

    /** 通知类型：1 回复、2 提及、3 文章被评论。 */
    private Integer type;

    /** 触发通知的用户 ID。 */
    private Long actorId;

    /** 关联文章 ID。 */
    private Long articleId;

    /** 关联评论 ID，用于跳转到具体讨论位置。 */
    private Long commentId;

    /** 评论内容摘要，最多 100 字符。 */
    private String contentPreview;

    /** 已读标记：0 未读，1 已读。 */
    private Integer readFlag;

    /** 通知生成时间。 */
    private LocalDateTime createdAt;
}
