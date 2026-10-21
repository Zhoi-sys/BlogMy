package com.blog.notification;

import com.blog.article.Article;
import com.blog.common.api.PageResult;
import com.blog.interaction.Comment;
import com.blog.interaction.dto.InteractionDtos;
import com.blog.security.UserPrincipal;

import java.util.Set;

/**
 * 站内通知领域服务接口。
 *
 * <p>通知来源于回复、@ 提及和文章被评论。同一评论对同一接收人只生成一条通知，
 * 优先级为：回复 > 提及 > 文章评论。具体实现见
 * {@code com.blog.notification.impl.NotificationServiceImpl}。</p>
 *
 * @since 1.0.0
 */
public interface NotificationService {

    /** 通知类型：回复了我的评论。 */
    int TYPE_REPLY = 1;

    /** 通知类型：在评论中提到了我。 */
    int TYPE_MENTION = 2;

    /** 通知类型：评论了我的文章。 */
    int TYPE_ARTICLE_COMMENT = 3;

    /**
     * 为一条新评论生成接收者通知。
     *
     * @param article        被评论文章
     * @param comment        新评论
     * @param actorId        评论者 ID，不给自己发通知
     * @param replyToUserId  被回复用户 ID，可为空
     * @param mentionUserIds 被提及用户 ID 集合
     */
    void notifyComment(Article article, Comment comment, Long actorId,
                       Long replyToUserId, Set<Long> mentionUserIds);

    /**
     * 分页查询当前用户通知。
     *
     * @param principal  当前登录用户
     * @param unreadOnly 是否只查询未读
     * @param cursor     游标
     * @param size       每页条数
     * @return 通知游标分页
     */
    PageResult<InteractionDtos.NotificationView> list(UserPrincipal principal, boolean unreadOnly,
                                                      String cursor, int size);

    /**
     * 标记指定通知或全部通知为已读。
     *
     * @param principal 当前登录用户
     * @param request   通知 ID 列表或 all=true
     * @return 实际更新条数
     */
    int markRead(UserPrincipal principal, InteractionDtos.ReadRequest request);

    /**
     * 查询未读通知数量。
     *
     * @param principal 当前登录用户
     * @return 未读数量
     */
    long unreadCount(UserPrincipal principal);
}
