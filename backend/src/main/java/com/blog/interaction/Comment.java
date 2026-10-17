package com.blog.interaction;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 评论实体。一期只有两级：parent_id=0 为一级评论，其余为回复。
 * 回复的 parent_id 始终指向一级评论，reply_to_user_id 保存被回复人。
 */
@Getter
@Setter
public class Comment {

    private Long id;
    private Long userId;
    private Long articleId;
    private Long parentId;
    private Long replyToUserId;
    private String content;
    private Integer likeCount;
    private Integer status;
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
