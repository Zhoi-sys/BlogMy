package com.blog.interaction;

import com.blog.interaction.dto.InteractionDtos;

/**
 * 点赞领域服务接口。
 *
 * <p>文章与评论共用点赞表，使用目标类型区分；点赞和取消均要求幂等。
 * 具体实现见 {@code com.blog.interaction.impl.LikeServiceImpl}。</p>
 *
 * @since 1.0.0
 */
public interface LikeService {

    /** 点赞目标类型：文章。 */
    int TARGET_ARTICLE = 1;

    /** 点赞目标类型：评论。 */
    int TARGET_COMMENT = 2;

    /**
     * 点赞文章或评论。
     *
     * @param userId  点赞用户 ID
     * @param request 目标类型与目标 ID
     * @return 当前点赞状态与最新点赞数
     */
    InteractionDtos.LikeResult like(Long userId, InteractionDtos.LikeRequest request);

    /**
     * 取消点赞。
     *
     * @param userId  操作用户 ID
     * @param request 目标类型与目标 ID
     * @return 当前点赞状态与最新点赞数
     */
    InteractionDtos.LikeResult unlike(Long userId, InteractionDtos.LikeRequest request);

    /**
     * 判断用户是否已点赞目标对象。
     *
     * @param userId     用户 ID，可为空
     * @param targetType 目标类型
     * @param targetId   目标 ID
     * @return 已点赞返回 {@code true}
     */
    boolean isLiked(Long userId, int targetType, Long targetId);
}
