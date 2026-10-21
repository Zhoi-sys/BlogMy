package com.blog.notification;

import com.blog.common.api.PageResult;
import com.blog.common.api.Result;
import com.blog.interaction.dto.InteractionDtos;
import com.blog.security.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 站内通知接口。
 *
 * <p>通知由回复、@ 提及和文章被评论三种事件产生，仅通知事件接收者本人。
 * 所有接口均需要登录。</p>
 *
 * @since 1.0.0
 */
@Validated
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 创建通知控制器。
     *
     * @param notificationService 通知服务
     */
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 分页查询当前用户通知。
     *
     * @param principal  当前登录用户
     * @param unreadOnly 是否只返回未读通知
     * @param cursor     游标
     * @param size       每页条数，1-30
     * @return 通知游标分页
     */
    @GetMapping
    public Result<PageResult<InteractionDtos.NotificationView>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(30) int size) {
        return Result.ok(notificationService.list(principal, unreadOnly, cursor, size));
    }

    /**
     * 将指定通知或全部通知标记为已读。
     *
     * @param principal 当前登录用户
     * @param request   ids 或 all=true
     * @return 标记后的未读数量
     */
    @PutMapping("/read")
    public Result<InteractionDtos.UnreadCount> markRead(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody InteractionDtos.ReadRequest request) {
        notificationService.markRead(principal, request);
        return Result.ok(new InteractionDtos.UnreadCount(notificationService.unreadCount(principal)));
    }

    /**
     * 查询当前用户未读通知数量，用于页头角标。
     *
     * @param principal 当前登录用户
     * @return 未读数量
     */
    @GetMapping("/unread-count")
    public Result<InteractionDtos.UnreadCount> unreadCount(@AuthenticationPrincipal UserPrincipal principal) {
        return Result.ok(new InteractionDtos.UnreadCount(notificationService.unreadCount(principal)));
    }
}
