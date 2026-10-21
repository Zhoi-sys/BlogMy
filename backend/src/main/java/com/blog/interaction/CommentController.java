package com.blog.interaction;

import com.blog.common.api.PageResult;
import com.blog.common.api.Result;
import com.blog.interaction.dto.InteractionDtos;
import com.blog.security.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评论与 @ 回复接口。
 *
 * <p>评论列表公开可读；发表和删除评论需要登录。评论采用两级结构：
 * 一级评论分页展示，每条一级评论默认携带前 3 条回复，更多回复通过
 * 回复分页接口展开。</p>
 *
 * @since 1.0.0
 */
@Validated
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    /**
     * 创建评论控制器。
     *
     * @param commentService 评论服务
     */
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 分页查询文章的一级评论。
     *
     * @param articleId 文章 ID
     * @param cursor    游标，首页不传
     * @param size      每页条数，1-30
     * @param principal 当前用户，用于返回点赞状态，可为空
     * @return 一级评论分页，包含回复预览与回复总数
     */
    @GetMapping
    public Result<PageResult<InteractionDtos.CommentView>> roots(
            @RequestParam @NotNull(message = "缺少文章 ID") Long articleId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") @Min(1) @Max(30) int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        return Result.ok(commentService.listRoot(articleId, cursor, size, principal));
    }

    /**
     * 展开某条一级评论下的回复。
     *
     * @param id        一级评论 ID
     * @param cursor    游标
     * @param size      每页条数，1-30
     * @param principal 当前用户，用于返回点赞状态，可为空
     * @return 回复分页，按对话时间升序
     */
    @GetMapping("/{id}/replies")
    public Result<PageResult<InteractionDtos.CommentView>> replies(
            @PathVariable Long id,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") @Min(1) @Max(30) int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        return Result.ok(commentService.listReplies(id, cursor, size, principal));
    }

    /**
     * 发表评论或回复。
     *
     * <p>回复时传入被回复评论的 ID；服务端自动换算两级 parentId，并自动 @ 被回复人。
     * 一次最多提及 10 位用户，评论正文最多 1000 字。</p>
     *
     * @param request   文章 ID、正文、可选回复目标与被提及用户 ID
     * @param principal 当前登录用户
     * @return 新评论视图
     */
    @PostMapping
    public Result<InteractionDtos.CommentView> create(
            @Valid @RequestBody InteractionDtos.CommentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return Result.ok(commentService.create(request, principal));
    }

    /**
     * 删除评论（软删除）。
     *
     * <p>评论作者可删除自己的评论，博主可删除任意评论。删除后保留占位与回复上下文，
     * 同时扣减文章评论计数。</p>
     *
     * @param id        评论 ID
     * @param principal 当前登录用户
     * @return 空成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        commentService.delete(id, principal);
        return Result.ok();
    }
}
