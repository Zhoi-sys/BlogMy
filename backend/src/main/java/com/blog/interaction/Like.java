package com.blog.interaction;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Like {

    private Long id;
    private Long userId;
    private Integer targetType;
    private Long targetId;
    private LocalDateTime createdAt;
}
