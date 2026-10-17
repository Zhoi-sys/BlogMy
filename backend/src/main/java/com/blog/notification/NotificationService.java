package com.blog.notification;

import com.blog.article.Article;
import com.blog.common.api.PageResult;
import com.blog.interaction.Comment;
import com.blog.interaction.dto.InteractionDtos;
import com.blog.security.UserPrincipal;

import java.util.Set;

/**
 * 站内通知领域服务接口。实现见 {@code com.blog.notification.impl.NotificationServiceImpl}。
 */
public interface NotificationService {

    int TYPE_REPLY = 1;
    int TYPE_MENTION = 2;
    int TYPE_ARTICLE_COMMENT = 3;

    void notifyComment(Article article, Comment comment, Long actorId,
                       Long replyToUserId, Set<Long> mentionUserIds);

    PageResult<InteractionDtos.NotificationView> list(UserPrincipal principal, boolean unreadOnly,
                                                      String cursor, int size);

    int markRead(UserPrincipal principal, InteractionDtos.ReadRequest request);

    long unreadCount(UserPrincipal principal);
}
