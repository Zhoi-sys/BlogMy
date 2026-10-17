package com.blog.article;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ArticleTag {

    private Long id;
    private Long articleId;
    private String tagName;
    private LocalDateTime createdAt;
}
