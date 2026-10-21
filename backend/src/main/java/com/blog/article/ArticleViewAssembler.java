package com.blog.article;

import com.blog.article.dto.ArticleDtos;
import com.blog.common.api.PageResult;
import com.blog.common.util.PageResults;
import com.blog.interaction.LikeService;
import com.blog.security.UserPrincipal;
import com.blog.user.User;
import com.blog.user.UserMapper;
import com.blog.user.UserViewMapper;
import com.blog.user.dto.UserDtos;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文章读模型组装器。
 *
 * <p>负责批量加载标签与作者、计算点赞状态、提取目录，并组装文章卡片和详情，
 * 避免业务 Service 被 DTO 拼装细节淹没。</p>
 *
 * @since 1.0.0
 */
@Component
public class ArticleViewAssembler {

    private final ArticleTagMapper tagMapper;
    private final UserMapper userMapper;
    private final UserViewMapper userViewMapper;
    private final MarkdownRenderer renderer;
    private final LikeService likeService;

    /**
     * 创建 {@code ArticleViewAssembler} 实例，依赖由 Spring 注入。
     * @param tagMapper 依赖组件
     * @param userMapper 依赖组件
     * @param userViewMapper 依赖组件
     * @param renderer 依赖组件
     * @param likeService 依赖组件
     */
    public ArticleViewAssembler(ArticleTagMapper tagMapper, UserMapper userMapper, UserViewMapper userViewMapper, MarkdownRenderer renderer, LikeService likeService) {
        this.tagMapper = tagMapper;
        this.userMapper = userMapper;
        this.userViewMapper = userViewMapper;
        this.renderer = renderer;
        this.likeService = likeService;
    }

    /**
     * 将“多取一条”的文章查询结果裁剪为文章卡片分页。
     *
     * @param fetchedRows 查询结果，通常为 pageSize + 1 条
     * @param pageSize    每页条数
     * @param nextCursor  根据最后一条生成下一页游标的函数
     * @return 文章卡片分页
     */
    public PageResult<ArticleDtos.ArticleCard> toCardPage(List<Article> fetchedRows, int pageSize,
                                                          Function<Article, String> nextCursor) {
        return PageResults.slice(fetchedRows, pageSize, nextCursor, this::toCards);
    }

    /**
     * 批量组装文章卡片。
     *
     * @param articles 文章实体列表
     * @return 文章卡片列表
     */
    public List<ArticleDtos.ArticleCard> toCards(List<Article> articles) {
        Map<Long, List<String>> tags = loadTags(articles.stream().map(Article::getId).toList());
        Map<Long, UserDtos.UserSummary> authors = loadAuthors(articles);
        return articles.stream().map(article -> new ArticleDtos.ArticleCard(
                article.getId(),
                article.getTitle(),
                article.getSummary(),
                tags.getOrDefault(article.getId(), List.of()),
                article.getStatus(),
                article.getPublishTime(),
                article.getCreatedAt(),
                article.getUpdatedAt(),
                article.getViewCount(),
                article.getLikeCount(),
                article.getCommentCount(),
                article.getReadingMinutes(),
                authors.get(article.getAuthorId())
        )).toList();
    }

    /**
     * 组装文章详情。
     *
     * @param article   文章实体
     * @param principal 当前用户，可为空
     * @param canEdit   当前用户是否可编辑
     * @return 文章详情，原始 Markdown 仅对可编辑者返回
     */
    public ArticleDtos.ArticleDetail toDetail(Article article, UserPrincipal principal, boolean canEdit) {
        List<String> tags = loadTags(List.of(article.getId())).getOrDefault(article.getId(), List.of());
        User author = userMapper.selectById(article.getAuthorId());
        boolean liked = principal != null
                && likeService.isLiked(principal.id(), LikeService.TARGET_ARTICLE, article.getId());
        List<ArticleDtos.TocItem> toc = renderer.extractToc(article.getContentHtml()).stream()
                .map(item -> new ArticleDtos.TocItem(item.level(), item.id(), item.text()))
                .toList();
        return new ArticleDtos.ArticleDetail(
                article.getId(), article.getTitle(), article.getSummary(),
                article.getContentHtml(), canEdit ? article.getContentMd() : null,
                tags, article.getStatus(), article.getPublishTime(),
                article.getCreatedAt(), article.getUpdatedAt(),
                article.getViewCount(), article.getLikeCount(), article.getCommentCount(),
                article.getReadingMinutes(), liked, canEdit,
                author == null ? null : userViewMapper.toSummary(author), toc);
    }

    /**
     * 批量加载文章标签并按文章 ID 分组。
     *
     * @param articleIds 文章 ID 集合
     * @return 文章 ID 到标签名称列表的映射
     */
    private Map<Long, List<String>> loadTags(Collection<Long> articleIds) {
        if (articleIds.isEmpty()) {
            return Map.of();
        }
        return tagMapper.selectByArticleIds(articleIds).stream()
                .collect(Collectors.groupingBy(ArticleTag::getArticleId, LinkedHashMap::new,
                        Collectors.mapping(ArticleTag::getTagName, Collectors.toList())));
    }

    /**
     * 批量加载文章作者摘要。
     *
     * @param articles 文章列表
     * @return 作者 ID 到摘要的映射
     */
    private Map<Long, UserDtos.UserSummary> loadAuthors(List<Article> articles) {
        List<Long> ids = articles.stream().map(Article::getAuthorId).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectByIds(ids).stream()
                .map(userViewMapper::toSummary)
                .collect(Collectors.toMap(UserDtos.UserSummary::id, Function.identity(),
                        (a, b) -> a, HashMap::new));
    }
}
