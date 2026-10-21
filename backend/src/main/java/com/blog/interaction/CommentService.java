package com.blog.interaction;

import com.blog.common.api.PageResult;
import com.blog.interaction.dto.InteractionDtos;
import com.blog.security.UserPrincipal;

/**
 * 评论与 @ 回复领域服务接口。
 *
 * <p>评论固定为两级结构；回复始终挂载到一级评论下，并通过 replyToUserId 记录
 * 具体被回复人。具体实现见 {@code com.blog.interaction.impl.CommentServiceImpl}。</p>
 *
 * @since 1.0.0
 */
public interface CommentService {

    /**
     * 分页查询一级评论，并附带前三条回复预览。
     *
     * @param articleId 文章 ID
     * @param cursor    游标
     * @param size      每页条数
     * @param principal 当前用户，用于点赞状态，可为空
     * @return 一级评论分页
     */
    PageResult<InteractionDtos.CommentView> listRoot(Long articleId, String cursor, int size,
                                                     UserPrincipal principal);

    /**
     * 分页展开一级评论下的回复。
     *
     * @param rootId    一级评论 ID
     * @param cursor    游标
     * @param size      每页条数
     * @param principal 当前用户，用于点赞状态，可为空
     * @return 按对话时间升序的回复分页
     */
    PageResult<InteractionDtos.CommentView> listReplies(Long rootId, String cursor, int size,
                                                        UserPrincipal principal);

    /**
     * 发表评论或回复。
     *
     * <p>服务端负责敏感词校验、@ 标记规范化、mention 关系、文章计数和通知写入。</p>
     *
     * @param request   评论内容与回复目标
     * @param principal 当前登录用户
     * @return 新评论视图
     */
    InteractionDtos.CommentView create(InteractionDtos.CommentRequest request, UserPrincipal principal);

    /**
     * 软删除评论。
     *
     * @param id        评论 ID
     * @param principal 当前登录用户
     */
    void delete(Long id, UserPrincipal principal);
}
