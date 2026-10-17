package com.blog.article.dto;

import com.blog.user.dto.UserDtos;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public final class ArticleDtos {

    private ArticleDtos() {
    }

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

    public record ArticleStatusRequest(
            @NotBlank(message = "缺少操作类型")
            @Pattern(regexp = "publish|offline|delete", message = "不支持的状态操作")
            String action
    ) {
    }

    public record TocItem(int level, String id, String text) {
    }

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

    public record ArticleRef(Long id, int status) {
    }

    public record SiteView(
            String title,
            String description,
            String siteUrl,
            UserDtos.UserSummary owner,
            UserDtos.UserStats stats
    ) {
    }
}
