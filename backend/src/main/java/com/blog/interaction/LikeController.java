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

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PutMapping
    public Result<InteractionDtos.LikeResult> like(@AuthenticationPrincipal UserPrincipal principal,
                                                    @Valid @RequestBody InteractionDtos.LikeRequest request) {
        return Result.ok(likeService.like(principal.id(), request));
    }

    @DeleteMapping
    public Result<InteractionDtos.LikeResult> unlike(@AuthenticationPrincipal UserPrincipal principal,
                                                      @Valid @RequestBody InteractionDtos.LikeRequest request) {
        return Result.ok(likeService.unlike(principal.id(), request));
    }
}
