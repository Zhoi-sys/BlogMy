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

@Validated
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public Result<PageResult<InteractionDtos.NotificationView>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(30) int size) {
        return Result.ok(notificationService.list(principal, unreadOnly, cursor, size));
    }

    @PutMapping("/read")
    public Result<InteractionDtos.UnreadCount> markRead(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody InteractionDtos.ReadRequest request) {
        notificationService.markRead(principal, request);
        return Result.ok(new InteractionDtos.UnreadCount(notificationService.unreadCount(principal)));
    }

    @GetMapping("/unread-count")
    public Result<InteractionDtos.UnreadCount> unreadCount(@AuthenticationPrincipal UserPrincipal principal) {
        return Result.ok(new InteractionDtos.UnreadCount(notificationService.unreadCount(principal)));
    }
}
