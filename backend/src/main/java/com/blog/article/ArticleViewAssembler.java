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
 * 文章读模型组装器：批量加载标签/作者、组装卡片与详情，避免业务 Service 被 DTO 拼装淹没。
 */
@Component
public class ArticleViewAssembler {

    private final ArticleTagMapper tagMapper;
    private final UserMapper userMapper;
    private final UserViewMapper userViewMapper;
    private final MarkdownRenderer renderer;
    private final LikeService likeService;

    public ArticleViewAssembler(ArticleTagMapper tagMapper, UserMapper userMapper,
                                UserViewMapper userViewMapper, MarkdownRenderer renderer,
                                LikeService likeService) {
        this.tagMapper = tagMapper;
        this.userMapper = userMapper;
        this.userViewMapper = userViewMapper;
        this.renderer = renderer;
        this.likeService = likeService;
    }

    public PageResult<ArticleDtos.ArticleCard> toCardPage(List<Article> fetchedRows, int pageSize,
                                                          Function<Article, String> nextCursor) {
        return PageResults.slice(fetchedRows, pageSize, nextCursor, this::toCards);
    }

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

    private Map<Long, List<String>> loadTags(Collection<Long> articleIds) {
        if (articleIds.isEmpty()) {
            return Map.of();
        }
        return tagMapper.selectByArticleIds(articleIds).stream()
                .collect(Collectors.groupingBy(ArticleTag::getArticleId, LinkedHashMap::new,
                        Collectors.mapping(ArticleTag::getTagName, Collectors.toList())));
    }

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
