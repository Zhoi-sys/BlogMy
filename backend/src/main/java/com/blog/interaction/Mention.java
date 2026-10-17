package com.blog.interaction;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Mention {

    private Long id;
    private Long commentId;
    private Long fromUserId;
    private Long toUserId;
    private Integer readFlag;
    private LocalDateTime createdAt;
}
