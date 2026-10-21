package com.blog.interaction;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * {@literal @} 提及关系实体，对应 {@code mentions} 表。
 *
 * <p>记录评论中显式 @ 与被回复人的关系；同一评论对同一用户由唯一索引保证
 * 只有一条记录。</p>
 *
 * @since 1.0.0
 */
@Getter
@Setter
public class Mention {

    /** 提及关系主键。 */
    private Long id;

    /** 触发提及的评论 ID。 */
    private Long commentId;

    /** 发起提及的用户 ID。 */
    private Long fromUserId;

    /** 被提及的用户 ID。 */
    private Long toUserId;

    /** 提及消息已读标记：0 未读，1 已读。 */
    private Integer readFlag;

    /** 提及创建时间。 */
    private LocalDateTime createdAt;
}
