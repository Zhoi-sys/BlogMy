package com.blog.article;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 文章实体，对应 {@code articles} 表。
 *
 * <p>{@code contentMd} 是唯一内容源，{@code contentHtml} 是保存/发布时生成的
 * 安全 HTML 快照。状态 0 为草稿、1 为已发布、2 为已下架；{@code deleted} 为
 * 逻辑删除标记。SQL 映射见 {@code resources/mapper/ArticleMapper.xml}。</p>
 *
 * @since 1.0.0
 */
@Getter
@Setter
public class Article {

    /** 文章主键。 */
    private Long id;

    /** 作者用户 ID，目前固定为博主账号。 */
    private Long authorId;

    /** 文章标题，最多 80 字符。 */
    private String title;

    /** 摘要；为空时由服务端从正文截取。 */
    private String summary;

    /** 原始 Markdown 正文，是唯一内容源。 */
    private String contentMd;

    /** 服务端渲染并清洗后的安全 HTML 快照，用于列表详情与 SEO。 */
    private String contentHtml;

    /** 文章状态：0 草稿、1 已发布、2 已下架。 */
    private Integer status;

    /** 阅读数，按 IP 与文章做 1 小时去重后原子累加。 */
    private Integer viewCount;

    /** 点赞数快照，由点赞事务原子维护。 */
    private Integer likeCount;

    /** 有效评论数快照，评论新增/删除时原子维护。 */
    private Integer commentCount;

    /** 预计阅读分钟数，保存或发布时计算。 */
    private Integer readingMinutes;

    /** 首次发布时间；草稿为 {@code null}，重新发布时刷新。 */
    private LocalDateTime publishTime;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间，编辑、状态流转时刷新。 */
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0 正常，1 已删除。 */
    private Integer deleted;
}
