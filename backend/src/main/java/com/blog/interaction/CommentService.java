package com.blog.interaction;

import com.blog.common.api.PageResult;
import com.blog.interaction.dto.InteractionDtos;
import com.blog.security.UserPrincipal;

/**
 * 评论领域服务接口。实现见 {@code com.blog.interaction.impl.CommentServiceImpl}。
 */
public interface CommentService {

    PageResult<InteractionDtos.CommentView> listRoot(Long articleId, String cursor, int size,
                                                     UserPrincipal principal);

    PageResult<InteractionDtos.CommentView> listReplies(Long rootId, String cursor, int size,
                                                        UserPrincipal principal);

    InteractionDtos.CommentView create(InteractionDtos.CommentRequest request, UserPrincipal principal);

    void delete(Long id, UserPrincipal principal);
}
