package com.blog.interaction;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 点赞记录实体，对应 {@code likes} 表。
 *
 * <p>{@code (user_id, target_type, target_id)} 唯一索引保证同一用户对同一目标
 * 最多一条点赞记录。目标类型 1 为文章、2 为评论。</p>
 *
 * @since 1.0.0
 */
@Getter
@Setter
public class Like {

    /** 点赞记录主键。 */
    private Long id;

    /** 点赞用户 ID。 */
    private Long userId;

    /** 目标类型：1 文章，2 评论。 */
    private Integer targetType;

    /** 目标对象 ID，与 targetType 联合定位文章或评论。 */
    private Long targetId;

    /** 点赞时间。 */
    private LocalDateTime createdAt;
}
