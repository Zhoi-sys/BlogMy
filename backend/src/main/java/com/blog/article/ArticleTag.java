package com.blog.article;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 文章标签实体，对应 {@code article_tags} 表。
 *
 * <p>一期每篇文章最多 5 个字符串标签，发布时整组替换。</p>
 *
 * @since 1.0.0
 */
@Getter
@Setter
public class ArticleTag {

    /** 标签主键。 */
    private Long id;

    /** 所属文章 ID。 */
    private Long articleId;

    /** 标签名称，最多 32 字符，同一文章内不重复。 */
    private String tagName;

    /** 标签创建时间。 */
    private LocalDateTime createdAt;
}
