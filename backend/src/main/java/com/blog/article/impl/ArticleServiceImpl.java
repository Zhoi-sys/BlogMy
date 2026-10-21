package com.blog.article.impl;

import com.blog.article.Article;
import com.blog.article.ArticleMapper;
import com.blog.article.ArticleService;
import com.blog.article.ArticleTag;
import com.blog.article.ArticleTagMapper;
import com.blog.article.ArticleViewAssembler;
import com.blog.article.MarkdownRenderer;
import com.blog.article.dto.ArticleDtos;
import com.blog.common.api.PageResult;
import com.blog.common.exception.BusinessException;
import com.blog.common.store.KeyValueStore;
import com.blog.common.util.CursorCodec;
import com.blog.common.util.TextUtils;
import com.blog.security.UserPrincipal;
import com.blog.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 文章领域服务实现。
 *
 * <p>负责文章查询、内容渲染、标签规范化、发布状态机与阅读量防刷。写入操作在
 * 事务内完成文章落库与标签替换；详情读取只对作者暴露原始 Markdown，公开响应
 * 使用服务端清洗后的 HTML。</p>
 *
 * @since 1.0.0
 */
@Service
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper tagMapper;
    private final UserService userService;
    private final ArticleViewAssembler viewAssembler;
    private final MarkdownRenderer renderer;
    private final CursorCodec cursorCodec;
    private final KeyValueStore store;

    /**
     * 创建 {@code ArticleServiceImpl} 实例，依赖由 Spring 注入。
     * @param articleMapper 依赖组件
     * @param tagMapper 依赖组件
     * @param userService 依赖组件
     * @param viewAssembler 依赖组件
     * @param renderer 依赖组件
     * @param cursorCodec 依赖组件
     * @param store 依赖组件
     */
    public ArticleServiceImpl(ArticleMapper articleMapper, ArticleTagMapper tagMapper, UserService userService, ArticleViewAssembler viewAssembler, MarkdownRenderer renderer, CursorCodec cursorCodec, KeyValueStore store) {
        this.articleMapper = articleMapper;
        this.tagMapper = tagMapper;
        this.userService = userService;
        this.viewAssembler = viewAssembler;
        this.renderer = renderer;
        this.cursorCodec = cursorCodec;
        this.store = store;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResult<ArticleDtos.ArticleCard> feed(String keyword, String cursor, int size) {
        int fetchSize = fetchSize(size);
        CursorCodec.Cursor decoded = cursorCodec.decode(cursor);
        List<Article> rows = articleMapper.selectFeed(
                keyword == null ? "" : keyword.strip(),
                time(decoded), id(decoded), fetchSize);
        return viewAssembler.toCardPage(rows, size,
                article -> cursorCodec.encode(article.getPublishTime(), article.getId()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResult<ArticleDtos.ArticleCard> listByAuthor(Long authorId, Integer status, String cursor,
                                                            int size, UserPrincipal principal) {
        userService.requireActive(authorId);
        boolean owner = principal != null && principal.id().equals(authorId);
        int effectiveStatus = status == null ? 1 : status;
        validateStatus(effectiveStatus);
        if (!owner && effectiveStatus != 1) {
            throw BusinessException.forbidden("只能查看已发布的文章");
        }

        CursorCodec.Cursor decoded = cursorCodec.decode(cursor);
        int fetchSize = fetchSize(size);
        boolean byPublishTime = effectiveStatus == 1;
        List<Article> rows = byPublishTime
                ? articleMapper.selectPublishedByAuthor(authorId, time(decoded), id(decoded), fetchSize)
                : articleMapper.selectAuthorArticlesByStatus(
                        authorId, effectiveStatus, time(decoded), id(decoded), fetchSize);
        return viewAssembler.toCardPage(rows, size, article -> cursorCodec.encode(
                byPublishTime ? article.getPublishTime() : article.getCreatedAt(), article.getId()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResult<ArticleDtos.ArticleCard> manage(Integer status, String cursor, int size,
                                                      UserPrincipal principal) {
        if (status != null) {
            validateStatus(status);
        }
        CursorCodec.Cursor decoded = cursorCodec.decode(cursor);
        List<Article> rows = articleMapper.selectManage(
                principal.id(), status, time(decoded), id(decoded), fetchSize(size));
        return viewAssembler.toCardPage(rows, size,
                article -> cursorCodec.encode(article.getUpdatedAt(), article.getId()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArticleDtos.ArticleDetail detail(Long id, UserPrincipal principal, String ip) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw BusinessException.notFound("文章不存在");
        }
        boolean canEdit = canEdit(article, principal);
        if (article.getStatus() != 1 && !canEdit) {
            throw BusinessException.notFound("文章不存在");
        }

        // 阅读量防刷：同一 IP、同一文章 1 小时内只计一次；作者预览不计入。
        if (article.getStatus() == 1 && !canEdit && article.getPublishTime() != null
                && store.setIfAbsent("view:article:" + id + ":" + ip, "1", Duration.ofHours(1))) {
            articleMapper.incrementViewCount(id);
            article.setViewCount(article.getViewCount() + 1);
        }
        return viewAssembler.toDetail(article, principal, canEdit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ArticleDtos.ArticleRef create(ArticleDtos.ArticleRequest request, UserPrincipal principal) {
        int status = request.status() == null ? 0 : request.status();
        if (status != 0 && status != 1) {
            throw BusinessException.badRequest("文章状态不正确");
        }
        List<String> tags = normalizeTags(request.tags());
        validateForStatus(status, request.contentMd(), tags);
        LocalDateTime now = LocalDateTime.now();

        Article article = new Article();
        article.setAuthorId(principal.id());
        article.setTitle(requireTitle(request.title()));
        article.setStatus(status);
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCommentCount(0);
        article.setPublishTime(status == 1 ? now : null);
        article.setCreatedAt(now);
        article.setUpdatedAt(now);
        article.setDeleted(0);
        applyRenderedContent(article, request.summary(), request.contentMd());
        articleMapper.insert(article);
        replaceTags(article.getId(), tags, now);
        return new ArticleDtos.ArticleRef(article.getId(), article.getStatus());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ArticleDtos.ArticleRef update(Long id, ArticleDtos.ArticleRequest request, UserPrincipal principal) {
        Article article = requireEditable(id, principal);
        List<String> tags = normalizeTags(request.tags());
        validateForStatus(article.getStatus(), request.contentMd(), tags);

        article.setTitle(requireTitle(request.title()));
        applyRenderedContent(article, request.summary(), request.contentMd());
        article.setUpdatedAt(LocalDateTime.now());
        articleMapper.update(article);
        replaceTags(article.getId(), tags, article.getUpdatedAt());
        return new ArticleDtos.ArticleRef(article.getId(), article.getStatus());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ArticleDtos.ArticleRef changeStatus(Long id, String action, UserPrincipal principal) {
        Article article = requireEditable(id, principal);
        LocalDateTime now = LocalDateTime.now();
        switch (action) {
            case "publish" -> publish(article, now);
            case "offline" -> offline(article, now);
            case "delete" -> {
                articleMapper.softDelete(id, now);
                article.setDeleted(1);
            }
            default -> throw BusinessException.badRequest("不支持的状态操作");
        }
        return new ArticleDtos.ArticleRef(id, article.getStatus());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long totalPublishedLikes() {
        return articleMapper.sumAllPublishedLikes();
    }

    private void publish(Article article, LocalDateTime now) {
        List<String> tags = loadTagNames(article.getId());
        validatePublish(article.getContentMd(), tags);
        if (article.getStatus() == 1) {
            return;
        }
        articleMapper.updateStatus(article.getId(), 1, now, now);
        article.setStatus(1);
        article.setPublishTime(now);
    }

    private void offline(Article article, LocalDateTime now) {
        if (article.getStatus() != 1) {
            throw BusinessException.conflict("只有已发布文章可以下架");
        }
        articleMapper.updateStatus(article.getId(), 2, article.getPublishTime(), now);
        article.setStatus(2);
    }

    private void applyRenderedContent(Article article, String summary, String markdown) {
        String source = markdown == null ? "" : markdown;
        MarkdownRenderer.RenderedDocument rendered = renderer.render(source);
        article.setContentMd(source);
        article.setContentHtml(rendered.html());
        article.setSummary(resolveSummary(summary, rendered.plainText()));
        article.setReadingMinutes(TextUtils.readingMinutes(rendered.plainText()));
    }

    private Article requireEditable(Long id, UserPrincipal principal) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw BusinessException.notFound("文章不存在");
        }
        if (!canEdit(article, principal)) {
            throw BusinessException.forbidden("没有权限编辑这篇文章");
        }
        return article;
    }

    private boolean canEdit(Article article, UserPrincipal principal) {
        return principal != null
                && (principal.isAdmin() || principal.id().equals(article.getAuthorId()));
    }

    private void validateForStatus(int status, String contentMd, List<String> tags) {
        if (status == 1) {
            validatePublish(contentMd, tags);
        }
    }

    private void validatePublish(String contentMd, List<String> tags) {
        if (contentMd == null || contentMd.isBlank()) {
            throw BusinessException.badRequest("发布前请填写文章正文");
        }
        if (contentMd.length() > 200_000) {
            throw BusinessException.badRequest("正文不能超过 20 万个字符");
        }
        if (tags.isEmpty()) {
            throw BusinessException.badRequest("发布前请至少添加 1 个标签");
        }
        if (tags.size() > 5) {
            throw BusinessException.badRequest("最多添加 5 个标签");
        }
    }

    private String requireTitle(String title) {
        String normalized = title == null ? "" : title.strip();
        if (normalized.isBlank()) {
            throw BusinessException.badRequest("请输入文章标题");
        }
        if (normalized.codePointCount(0, normalized.length()) > 80) {
            throw BusinessException.badRequest("标题不能超过 80 个字符");
        }
        return normalized;
    }

    private String resolveSummary(String summary, String plainText) {
        if (summary != null && !summary.isBlank()) {
            return TextUtils.truncate(summary, 255);
        }
        return TextUtils.truncate(TextUtils.normalizeWhitespace(plainText), 140);
    }

    /**
     * 标签去空格、去 #、忽略大小写去重，最多 5 个。
     */
    private List<String> normalizeTags(List<String> tags) {
        if (tags == null) {
            return List.of();
        }
        LinkedHashMap<String, String> unique = new LinkedHashMap<>();
        for (String tag : tags) {
            if (tag == null) {
                continue;
            }
            String normalized = tag.strip().replaceFirst("^#+", "").strip();
            if (normalized.isBlank()) {
                continue;
            }
            if (normalized.codePointCount(0, normalized.length()) > 32) {
                throw BusinessException.badRequest("单个标签不能超过 32 个字符");
            }
            unique.putIfAbsent(normalized.toLowerCase(Locale.ROOT), normalized);
            if (unique.size() > 5) {
                throw BusinessException.badRequest("最多添加 5 个标签");
            }
        }
        return new ArrayList<>(unique.values());
    }

    private void replaceTags(Long articleId, List<String> tags, LocalDateTime now) {
        tagMapper.deleteByArticleId(articleId);
        for (String name : tags) {
            ArticleTag tag = new ArticleTag();
            tag.setArticleId(articleId);
            tag.setTagName(name);
            tag.setCreatedAt(now);
            tagMapper.insert(tag);
        }
    }

    private List<String> loadTagNames(Long articleId) {
        return tagMapper.selectByArticleIds(List.of(articleId)).stream()
                .map(ArticleTag::getTagName)
                .toList();
    }

    private void validateStatus(int status) {
        if (status < 0 || status > 2) {
            throw BusinessException.badRequest("文章状态不正确");
        }
    }

    private int fetchSize(int size) {
        return Math.min(Math.max(size, 1), 30) + 1;
    }

    private LocalDateTime time(CursorCodec.Cursor cursor) {
        return cursor == null ? null : cursor.time();
    }

    private Long id(CursorCodec.Cursor cursor) {
        return cursor == null ? null : cursor.id();
    }
}
