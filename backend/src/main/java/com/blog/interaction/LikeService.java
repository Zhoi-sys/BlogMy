package com.blog.interaction;

import com.blog.interaction.dto.InteractionDtos;

/**
 * 点赞领域服务接口。实现见 {@code com.blog.interaction.impl.LikeServiceImpl}。
 */
public interface LikeService {

    int TARGET_ARTICLE = 1;
    int TARGET_COMMENT = 2;

    InteractionDtos.LikeResult like(Long userId, InteractionDtos.LikeRequest request);

    InteractionDtos.LikeResult unlike(Long userId, InteractionDtos.LikeRequest request);

    boolean isLiked(Long userId, int targetType, Long targetId);
}
