package com.blog.article;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 文章实体。content_md 是唯一的内容源，content_html 是发布时生成的安全 HTML 快照。
 * SQL 映射见 resources/mapper/ArticleMapper.xml。
 */
@Getter
@Setter
public class Article {

    private Long id;
    private Long authorId;
    private String title;
    private String summary;
    private String contentMd;
    private String contentHtml;
    private Integer status;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer readingMinutes;
    private LocalDateTime publishTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
