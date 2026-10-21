package com.blog.interaction;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 评论实体，对应 {@code comments} 表。
 *
 * <p>一期只有两级：{@code parent_id=0} 为一级评论，其余为回复；回复的
 * {@code parent_id} 始终指向一级评论，{@code replyToUserId} 保存具体被回复人。
 * 删除使用 {@code deleted=1} 软删除以保留上下文。</p>
 *
 * @since 1.0.0
 */
@Getter
@Setter
public class Comment {

    /** 评论主键。 */
    private Long id;

    /** 评论者用户 ID。 */
    private Long userId;

    /** 所属文章 ID。 */
    private Long articleId;

    /** 父级 ID：0 表示一级评论；回复时指向一级评论 ID。 */
    private Long parentId;

    /** 被回复用户 ID，用于展示“回复 @某人”并发送通知。 */
    private Long replyToUserId;

    /** 存储内容：纯文本 + 服务端生成的结构化 mention 标记。 */
    private String content;

    /** 评论点赞数快照。 */
    private Integer likeCount;

    /** 评论状态：1 正常，0 隐藏。 */
    private Integer status;

    /** 软删除标记：0 正常，1 已删除；删除后保留回复上下文。 */
    private Integer deleted;

    /** 发表时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
