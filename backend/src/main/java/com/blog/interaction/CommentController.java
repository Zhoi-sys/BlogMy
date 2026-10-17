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

@Validated
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public Result<PageResult<InteractionDtos.CommentView>> roots(
            @RequestParam @NotNull(message = "缺少文章 ID") Long articleId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") @Min(1) @Max(30) int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        return Result.ok(commentService.listRoot(articleId, cursor, size, principal));
    }

    @GetMapping("/{id}/replies")
    public Result<PageResult<InteractionDtos.CommentView>> replies(
            @PathVariable Long id,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") @Min(1) @Max(30) int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        return Result.ok(commentService.listReplies(id, cursor, size, principal));
    }

    @PostMapping
    public Result<InteractionDtos.CommentView> create(
            @Valid @RequestBody InteractionDtos.CommentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return Result.ok(commentService.create(request, principal));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        commentService.delete(id, principal);
        return Result.ok();
    }
}
