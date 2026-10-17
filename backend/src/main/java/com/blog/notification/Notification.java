package com.blog.notification;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Notification {

    private Long id;
    private Long userId;
    private Integer type;
    private Long actorId;
    private Long articleId;
    private Long commentId;
    private String contentPreview;
    private Integer readFlag;
    private LocalDateTime createdAt;
}
