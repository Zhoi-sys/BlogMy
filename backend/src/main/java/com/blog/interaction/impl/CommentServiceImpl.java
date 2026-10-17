package com.blog.interaction.impl;

import com.blog.article.Article;
import com.blog.article.ArticleMapper;
import com.blog.common.api.PageResult;
import com.blog.common.exception.BusinessException;
import com.blog.common.store.RateLimiter;
import com.blog.common.store.SensitiveWordFilter;
import com.blog.common.util.CursorCodec;
import com.blog.interaction.Comment;
import com.blog.interaction.CommentContentRenderer;
import com.blog.interaction.CommentMapper;
import com.blog.interaction.CommentService;
import com.blog.interaction.CommentViewAssembler;
import com.blog.interaction.Mention;
import com.blog.interaction.MentionMapper;
import com.blog.interaction.dto.InteractionDtos;
import com.blog.notification.NotificationService;
import com.blog.security.UserPrincipal;
import com.blog.user.User;
import com.blog.user.UserMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private static final int MAX_CONTENT_LENGTH = 1000;

    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final MentionMapper mentionMapper;
    private final NotificationService notificationService;
    private final SensitiveWordFilter sensitiveWordFilter;
    private final RateLimiter rateLimiter;
    private final CursorCodec cursorCodec;
    private final CommentContentRenderer contentRenderer;
    private final CommentViewAssembler viewAssembler;

    public CommentServiceImpl(CommentMapper commentMapper, ArticleMapper articleMapper, UserMapper userMapper,
                              MentionMapper mentionMapper, NotificationService notificationService,
                              SensitiveWordFilter sensitiveWordFilter, RateLimiter rateLimiter,
                              CursorCodec cursorCodec, CommentContentRenderer contentRenderer,
                              CommentViewAssembler viewAssembler) {
        this.commentMapper = commentMapper;
        this.articleMapper = articleMapper;
        this.userMapper = userMapper;
        this.mentionMapper = mentionMapper;
        this.notificationService = notificationService;
        this.sensitiveWordFilter = sensitiveWordFilter;
        this.rateLimiter = rateLimiter;
        this.cursorCodec = cursorCodec;
        this.contentRenderer = contentRenderer;
        this.viewAssembler = viewAssembler;
    }

    @Override
    public PageResult<InteractionDtos.CommentView> listRoot(Long articleId, String cursor, int size,
                                                             UserPrincipal principal) {
        requirePublishedArticle(articleId);
        CursorCodec.Cursor decoded = cursorCodec.decode(cursor);
        List<Comment> rows = commentMapper.selectRoots(
                articleId, time(decoded), id(decoded), fetchSize(size));
        return viewAssembler.assembleRoots(rows, size, principal);
    }

    @Override
    public PageResult<InteractionDtos.CommentView> listReplies(Long rootId, String cursor, int size,
                                                                UserPrincipal principal) {
        Comment root = commentMapper.selectById(rootId);
        if (root == null || root.getParentId() != 0 || root.getStatus() != 1) {
            throw BusinessException.notFound("评论不存在");
        }
        requirePublishedArticle(root.getArticleId());
        CursorCodec.Cursor decoded = cursorCodec.decode(cursor);
        List<Comment> rows = commentMapper.selectReplies(
                rootId, time(decoded), id(decoded), fetchSize(size));
        return viewAssembler.assembleReplies(rows, size, principal);
    }

    /**
     * 发表评论/回复。一个事务内完成评论落库、文章计数、mention 关系和站内通知。
     */
    @Override
    @Transactional
    public InteractionDtos.CommentView create(InteractionDtos.CommentRequest request, UserPrincipal principal) {
        rateLimiter.check("comment-user", principal.id().toString(), 10, Duration.ofMinutes(1));
        Article article = requirePublishedArticle(request.articleId());
        String content = normalizeContent(request.content());
        sensitiveWordFilter.validate(content);

        Comment target = resolveReplyTarget(request, article);
        long parentId = target == null ? 0L
                : (target.getParentId() == 0 ? target.getId() : target.getParentId());
        List<User> mentionUsers = loadMentionUsers(request.mentionUserIds());
        MentionContent mentionContent = buildStoredContent(content, mentionUsers, target);
        LocalDateTime now = LocalDateTime.now();

        Comment comment = new Comment();
        comment.setUserId(principal.id());
        comment.setArticleId(article.getId());
        comment.setParentId(parentId);
        comment.setReplyToUserId(target == null ? null : target.getUserId());
        comment.setContent(mentionContent.content());
        comment.setLikeCount(0);
        comment.setStatus(1);
        comment.setDeleted(0);
        comment.setCreatedAt(now);
        comment.setUpdatedAt(now);
        commentMapper.insert(comment);
        articleMapper.incrementCommentCount(article.getId());

        saveMentions(comment, principal.id(), mentionContent.mentionedUserIds(), now);
        notificationService.notifyComment(
                article, comment, principal.id(),
                target == null ? null : target.getUserId(),
                mentionContent.mentionedUserIds());
        return viewAssembler.assembleSingle(comment, principal);
    }

    @Override
    @Transactional
    public void delete(Long id, UserPrincipal principal) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw BusinessException.notFound("评论不存在");
        }
        if (!principal.isAdmin() && !principal.id().equals(comment.getUserId())) {
            throw BusinessException.forbidden("没有权限删除这条评论");
        }
        if (comment.getDeleted() == 1) {
            return;
        }
        commentMapper.updateDeleted(id, LocalDateTime.now());
        articleMapper.decrementCommentCount(comment.getArticleId());
    }

    private Comment resolveReplyTarget(InteractionDtos.CommentRequest request, Article article) {
        if (request.parentId() == null || request.parentId() <= 0) {
            return null;
        }
        Comment target = commentMapper.selectById(request.parentId());
        if (target == null) {
            throw BusinessException.notFound("被回复的评论不存在");
        }
        if (!target.getArticleId().equals(article.getId())) {
            throw BusinessException.badRequest("评论不属于当前文章");
        }
        if (target.getDeleted() == 1 || target.getStatus() != 1) {
            throw BusinessException.conflict("该评论已删除，无法回复");
        }
        return target;
    }

    private void saveMentions(Comment comment, Long actorId, Set<Long> mentionedUserIds, LocalDateTime now) {
        for (Long userId : mentionedUserIds) {
            if (userId.equals(actorId)) {
                continue;
            }
            Mention mention = new Mention();
            mention.setCommentId(comment.getId());
            mention.setFromUserId(actorId);
            mention.setToUserId(userId);
            mention.setReadFlag(0);
            mention.setCreatedAt(now);
            try {
                mentionMapper.insert(mention);
            } catch (DuplicateKeyException ignored) {
                // 同一用户既是被回复人又是显式 @ 时，只保留一条 mention。
            }
        }
    }

    private String normalizeContent(String raw) {
        String content = raw == null ? "" : raw.replace("\r\n", "\n").strip();
        if (content.isBlank()) {
            throw BusinessException.badRequest("评论内容不能为空");
        }
        if (content.codePointCount(0, content.length()) > MAX_CONTENT_LENGTH) {
            throw BusinessException.badRequest("评论不能超过 1000 个字符");
        }
        // 防止客户端伪造结构化 mention 标记，mention 只能由服务端生成。
        return content.replace("](uid:", "](uid：");
    }

    /**
     * 将用户选中的 @昵称 转为结构化标记，并为被回复人自动补 @。
     */
    private MentionContent buildStoredContent(String content, List<User> mentionUsers, Comment replyTarget) {
        String stored = content;
        Set<Long> mentionedIds = new LinkedHashSet<>();
        List<User> candidates = new ArrayList<>(mentionUsers);
        if (replyTarget != null) {
            User targetAuthor = userMapper.selectById(replyTarget.getUserId());
            if (targetAuthor != null
                    && candidates.stream().noneMatch(user -> user.getId().equals(targetAuthor.getId()))) {
                candidates.add(targetAuthor);
            }
        }
        candidates.sort(Comparator.comparingInt((User user) -> user.getNickname().length()).reversed());

        for (User user : candidates) {
            Pattern pattern = mentionPattern(user.getNickname());
            Matcher matcher = pattern.matcher(stored);
            if (matcher.find()) {
                String marker = contentRenderer.marker(user.getNickname(), user.getId());
                stored = matcher.replaceAll(Matcher.quoteReplacement(marker));
                mentionedIds.add(user.getId());
            }
        }

        if (replyTarget != null && !mentionedIds.contains(replyTarget.getUserId())) {
            User targetAuthor = userMapper.selectById(replyTarget.getUserId());
            if (targetAuthor != null) {
                stored = contentRenderer.marker(targetAuthor.getNickname(), targetAuthor.getId())
                        + " " + stored;
                mentionedIds.add(targetAuthor.getId());
            }
        }
        return new MentionContent(stored, mentionedIds);
    }

    private Pattern mentionPattern(String nickname) {
        return Pattern.compile("(?<![\\p{L}\\p{N}_@])@" + Pattern.quote(nickname)
                + "(?![\\p{L}\\p{N}_])");
    }

    private List<User> loadMentionUsers(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<Long> distinct = ids.stream().filter(id -> id != null).distinct().toList();
        if (distinct.size() > 10) {
            throw BusinessException.badRequest("一次最多提及 10 位用户");
        }
        Map<Long, User> users = userMapper.selectByIds(distinct).stream()
                .filter(user -> user.getStatus() != null && user.getStatus() == 1)
                .collect(Collectors.toMap(User::getId, Function.identity()));
        if (users.size() != distinct.size()) {
            throw BusinessException.badRequest("部分被提及的用户不存在");
        }
        return distinct.stream().map(users::get).toList();
    }

    private Article requirePublishedArticle(Long articleId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null || article.getStatus() != 1) {
            throw BusinessException.notFound("文章不存在或已下架");
        }
        return article;
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

    private record MentionContent(String content, Set<Long> mentionedUserIds) {
    }
}
