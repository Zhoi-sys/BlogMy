package com.blog.interaction.impl;

import com.blog.article.Article;
import com.blog.article.ArticleMapper;
import com.blog.common.exception.BusinessException;
import com.blog.interaction.Comment;
import com.blog.interaction.CommentMapper;
import com.blog.interaction.Like;
import com.blog.interaction.LikeMapper;
import com.blog.interaction.LikeService;
import com.blog.interaction.dto.InteractionDtos;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 点赞领域服务实现。
 *
 * <p>文章与评论点赞共用一张表，唯一索引保证幂等。只有实际插入/删除成功时才
 * 调整目标计数，避免并发双击造成计数漂移。</p>
 *
 * @since 1.0.0
 */
@Service
public class LikeServiceImpl implements LikeService {

    private final LikeMapper likeMapper;
    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;

    /**
     * 创建 {@code LikeServiceImpl} 实例，依赖由 Spring 注入。
     * @param likeMapper 依赖组件
     * @param articleMapper 依赖组件
     * @param commentMapper 依赖组件
     */
    public LikeServiceImpl(LikeMapper likeMapper, ArticleMapper articleMapper, CommentMapper commentMapper) {
        this.likeMapper = likeMapper;
        this.articleMapper = articleMapper;
        this.commentMapper = commentMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public InteractionDtos.LikeResult like(Long userId, InteractionDtos.LikeRequest request) {
        validateType(request.targetType());
        return request.targetType() == TARGET_ARTICLE
                ? likeArticle(userId, request)
                : likeComment(userId, request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public InteractionDtos.LikeResult unlike(Long userId, InteractionDtos.LikeRequest request) {
        validateType(request.targetType());
        int removed = likeMapper.delete(userId, request.targetType(), request.targetId());
        if (request.targetType() == TARGET_ARTICLE) {
            if (removed > 0) {
                articleMapper.decrementLikeCount(request.targetId());
            }
            Article fresh = articleMapper.selectById(request.targetId());
            return new InteractionDtos.LikeResult(false, fresh == null ? 0 : fresh.getLikeCount());
        }
        if (removed > 0) {
            commentMapper.decrementLikeCount(request.targetId());
        }
        Comment fresh = commentMapper.selectById(request.targetId());
        return new InteractionDtos.LikeResult(false, fresh == null ? 0 : fresh.getLikeCount());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLiked(Long userId, int targetType, Long targetId) {
        return userId != null && likeMapper.countByUserAndTarget(userId, targetType, targetId) > 0;
    }

    /**
     * 唯一索引兜底并发双击，只有真正插入成功才自增计数。
     */
    private InteractionDtos.LikeResult likeArticle(Long userId, InteractionDtos.LikeRequest request) {
        Article article = requirePublishedArticle(request.targetId());
        if (likeMapper.countByUserAndTarget(userId, request.targetType(), request.targetId()) == 0) {
            try {
                insertLike(userId, request.targetType(), request.targetId());
                articleMapper.incrementLikeCount(article.getId());
            } catch (DuplicateKeyException ignored) {
                // 并发重复点赞，唯一索引已保证幂等。
            }
        }
        Article fresh = articleMapper.selectById(article.getId());
        return new InteractionDtos.LikeResult(true,
                fresh == null ? article.getLikeCount() : fresh.getLikeCount());
    }

    private InteractionDtos.LikeResult likeComment(Long userId, InteractionDtos.LikeRequest request) {
        Comment comment = requireActiveComment(request.targetId());
        requirePublishedArticle(comment.getArticleId());
        if (likeMapper.countByUserAndTarget(userId, request.targetType(), request.targetId()) == 0) {
            try {
                insertLike(userId, request.targetType(), request.targetId());
                commentMapper.incrementLikeCount(comment.getId());
            } catch (DuplicateKeyException ignored) {
                // 幂等。
            }
        }
        Comment fresh = commentMapper.selectById(comment.getId());
        return new InteractionDtos.LikeResult(true,
                fresh == null ? comment.getLikeCount() : fresh.getLikeCount());
    }

    private void insertLike(Long userId, int targetType, Long targetId) {
        Like like = new Like();
        like.setUserId(userId);
        like.setTargetType(targetType);
        like.setTargetId(targetId);
        like.setCreatedAt(LocalDateTime.now());
        likeMapper.insert(like);
    }

    private Article requirePublishedArticle(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null || article.getStatus() != 1) {
            throw BusinessException.notFound("文章不存在或已下架");
        }
        return article;
    }

    private Comment requireActiveComment(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null || comment.getDeleted() == 1 || comment.getStatus() != 1) {
            throw BusinessException.notFound("评论不存在或已删除");
        }
        return comment;
    }

    private void validateType(int targetType) {
        if (targetType != TARGET_ARTICLE && targetType != TARGET_COMMENT) {
            throw BusinessException.badRequest("不支持的点赞对象");
        }
    }
}
