package com.blog.interaction;

import com.blog.common.api.Result;
import com.blog.interaction.dto.InteractionDtos;
import com.blog.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 点赞接口。
 *
 * <p>支持文章与评论两种目标，均要求登录。点赞与取消点赞都是幂等操作，
 * 返回操作后的点赞状态和最新计数。</p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/likes")
public class LikeController {

    private final LikeService likeService;

    /**
     * 创建点赞控制器。
     *
     * @param likeService 点赞服务
     */
    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    /**
     * 点赞文章或评论。
     *
     * @param principal 当前登录用户
     * @param request   目标类型（1 文章、2 评论）与目标 ID
     * @return 已点赞状态与最新点赞数
     */
    @PutMapping
    public Result<InteractionDtos.LikeResult> like(@AuthenticationPrincipal UserPrincipal principal,
                                                    @Valid @RequestBody InteractionDtos.LikeRequest request) {
        return Result.ok(likeService.like(principal.id(), request));
    }

    /**
     * 取消点赞。
     *
     * @param principal 当前登录用户
     * @param request   目标类型与目标 ID
     * @return 未点赞状态与最新点赞数
     */
    @DeleteMapping
    public Result<InteractionDtos.LikeResult> unlike(@AuthenticationPrincipal UserPrincipal principal,
                                                      @Valid @RequestBody InteractionDtos.LikeRequest request) {
        return Result.ok(likeService.unlike(principal.id(), request));
    }
}
