package com.blog.notification.impl;

import com.blog.article.Article;
import com.blog.common.api.PageResult;
import com.blog.common.exception.BusinessException;
import com.blog.common.util.CursorCodec;
import com.blog.interaction.Comment;
import com.blog.interaction.CommentContentRenderer;
import com.blog.interaction.dto.InteractionDtos;
import com.blog.notification.Notification;
import com.blog.notification.NotificationMapper;
import com.blog.notification.NotificationService;
import com.blog.notification.NotificationViewAssembler;
import com.blog.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final NotificationViewAssembler viewAssembler;
    private final CommentContentRenderer contentRenderer;
    private final CursorCodec cursorCodec;

    public NotificationServiceImpl(NotificationMapper notificationMapper,
                                   NotificationViewAssembler viewAssembler,
                                   CommentContentRenderer contentRenderer,
                                   CursorCodec cursorCodec) {
        this.notificationMapper = notificationMapper;
        this.viewAssembler = viewAssembler;
        this.contentRenderer = contentRenderer;
        this.cursorCodec = cursorCodec;
    }

    /**
     * 同一评论对同一接收人只生成一条通知，优先级：回复 > 提及 > 评论了我的文章。
     */
    @Override
    public void notifyComment(Article article, Comment comment, Long actorId,
                              Long replyToUserId, Set<Long> mentionUserIds) {
        Map<Long, Integer> recipients = new LinkedHashMap<>();
        if (replyToUserId != null) {
            recipients.putIfAbsent(replyToUserId, TYPE_REPLY);
        }
        for (Long id : mentionUserIds) {
            recipients.putIfAbsent(id, TYPE_MENTION);
        }
        recipients.putIfAbsent(article.getAuthorId(), TYPE_ARTICLE_COMMENT);
        recipients.remove(actorId);

        String preview = contentRenderer.preview(comment.getContent(), 100);
        LocalDateTime now = LocalDateTime.now();
        recipients.forEach((userId, type) -> {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setType(type);
            notification.setActorId(actorId);
            notification.setArticleId(article.getId());
            notification.setCommentId(comment.getId());
            notification.setContentPreview(preview);
            notification.setReadFlag(0);
            notification.setCreatedAt(now);
            notificationMapper.insert(notification);
        });
    }

    @Override
    public PageResult<InteractionDtos.NotificationView> list(UserPrincipal principal, boolean unreadOnly,
                                                              String cursor, int size) {
        CursorCodec.Cursor decoded = cursorCodec.decode(cursor);
        List<Notification> rows = notificationMapper.selectByUser(
                principal.id(), unreadOnly,
                decoded == null ? null : decoded.time(),
                decoded == null ? null : decoded.id(),
                Math.min(Math.max(size, 1), 30) + 1);
        return viewAssembler.toPage(rows, size);
    }

    @Override
    @Transactional
    public int markRead(UserPrincipal principal, InteractionDtos.ReadRequest request) {
        if (Boolean.TRUE.equals(request.all())) {
            return notificationMapper.markAllRead(principal.id());
        }
        if (request.ids() == null || request.ids().isEmpty()) {
            throw BusinessException.badRequest("请选择要标记的通知");
        }
        List<Long> ids = request.ids().stream().filter(id -> id != null).distinct().toList();
        if (ids.isEmpty()) {
            throw BusinessException.badRequest("请选择要标记的通知");
        }
        if (ids.size() > 100) {
            throw BusinessException.badRequest("一次最多标记 100 条通知");
        }
        return notificationMapper.markRead(principal.id(), ids);
    }

    @Override
    public long unreadCount(UserPrincipal principal) {
        return notificationMapper.countUnread(principal.id());
    }
}
