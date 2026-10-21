package com.blog.article.dto;

import com.blog.user.dto.UserDtos;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章、站点与目录相关 DTO 集合。
 *
 * @since 1.0.0
 */
public final class ArticleDtos {

    private ArticleDtos() {
    }

    /**
     * 新建或编辑文章请求。
     *
     * @param title     标题，最多 80 个字符
     * @param summary   摘要；为空时由服务端截取正文生成
     * @param contentMd Markdown 正文，最多 20 万字符
     * @param tags      标签，发布时需 1-5 个
     * @param status    新建时的状态：0 草稿、1 发布；编辑时忽略
     */
    public record ArticleRequest(
            @NotBlank(message = "请输入文章标题")
            @Size(max = 80, message = "标题不能超过 80 个字符")
            String title,
            @Size(max = 255, message = "摘要不能超过 255 个字符")
            String summary,
            @Size(max = 200_000, message = "正文不能超过 20 万个字符")
            String contentMd,
            List<@Size(max = 32, message = "单个标签不能超过 32 个字符") String> tags,
            Integer status
    ) {
    }

    /**
     * 文章状态流转请求。
     *
     * @param action 动作：publish 发布、offline 下架、delete 删除
     */
    public record ArticleStatusRequest(
            @NotBlank(message = "缺少操作类型")
            @Pattern(regexp = "publish|offline|delete", message = "不支持的状态操作")
            String action
    ) {
    }

    /**
     * 文章目录项。
     *
     * @param level 标题层级，2 或 3
     * @param id    标题锚点 ID
     * @param text  标题纯文本
     */
    public record TocItem(int level, String id, String text) {
    }

    /**
     * 文章列表卡片。
     *
     * @param id            文章 ID
     * @param title         标题
     * @param summary       摘要
     * @param tags          标签列表
     * @param status        状态：0 草稿、1 已发布、2 已下架
     * @param publishTime   发布时间，草稿为 {@code null}
     * @param createdAt     创建时间
     * @param updatedAt     最后更新时间
     * @param viewCount     阅读数
     * @param likeCount     点赞数
     * @param commentCount  评论数
     * @param readingMinutes 预计阅读分钟数
     * @param author        作者摘要
     */
    public record ArticleCard(
            Long id,
            String title,
            String summary,
            List<String> tags,
            int status,
            LocalDateTime publishTime,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            int viewCount,
            int likeCount,
            int commentCount,
            int readingMinutes,
            UserDtos.UserSummary author
    ) {
    }

    /**
     * 文章详情。
     *
     * @param id            文章 ID
     * @param title         标题
     * @param summary       摘要
     * @param contentHtml   服务端渲染并清洗后的正文 HTML
     * @param contentMd     原始 Markdown，仅作者可获取
     * @param tags          标签列表
     * @param status        状态：0 草稿、1 已发布、2 已下架
     * @param publishTime   发布时间
     * @param createdAt     创建时间
     * @param updatedAt     最后更新时间
     * @param viewCount     阅读数
     * @param likeCount     点赞数
     * @param commentCount  评论数
     * @param readingMinutes 预计阅读分钟数
     * @param liked         当前用户是否已点赞
     * @param canEdit       当前用户是否可编辑
     * @param author        作者摘要
     * @param toc           目录项列表
     */
    public record ArticleDetail(
            Long id,
            String title,
            String summary,
            String contentHtml,
            String contentMd,
            List<String> tags,
            int status,
            LocalDateTime publishTime,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            int viewCount,
            int likeCount,
            int commentCount,
            int readingMinutes,
            boolean liked,
            boolean canEdit,
            UserDtos.UserSummary author,
            List<TocItem> toc
    ) {
    }

    /**
     * 文章创建或编辑后的轻量引用。
     *
     * @param id     文章 ID
     * @param status 当前文章状态
     */
    public record ArticleRef(Long id, int status) {
    }

    /**
     * 站点公开聚合信息。
     *
     * @param title       站点标题
     * @param description 站点简介
     * @param siteUrl     站点公开地址
     * @param owner       博主摘要
     * @param stats       博主公开统计
     */
    public record SiteView(
            String title,
            String description,
            String siteUrl,
            UserDtos.UserSummary owner,
            UserDtos.UserStats stats
    ) {
    }
}
