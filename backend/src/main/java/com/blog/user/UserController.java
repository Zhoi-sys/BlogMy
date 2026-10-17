package com.blog.user;

import com.blog.article.ArticleService;
import com.blog.article.dto.ArticleDtos;
import com.blog.common.api.PageResult;
import com.blog.common.api.Result;
import com.blog.security.UserPrincipal;
import com.blog.user.dto.UserDtos;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final ArticleService articleService;

    public UserController(UserService userService, ArticleService articleService) {
        this.userService = userService;
        this.articleService = articleService;
    }

    @GetMapping("/{id}")
    public Result<UserDtos.UserProfile> profile(@PathVariable Long id) {
        return Result.ok(userService.getProfile(id));
    }

    @GetMapping("/{id}/articles")
    public Result<PageResult<ArticleDtos.ArticleCard>> articles(
            @PathVariable Long id,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") @Min(1) @Max(30) int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        return Result.ok(articleService.listByAuthor(id, status, cursor, size, principal));
    }

    @PutMapping("/me")
    public Result<UserDtos.UserView> updateMe(@AuthenticationPrincipal UserPrincipal principal,
                                              @Valid @RequestBody UserDtos.UpdateProfileRequest request) {
        return Result.ok(userService.updateProfile(principal, request));
    }

    @GetMapping("/search")
    public Result<List<UserDtos.UserSummary>> search(
            @RequestParam @NotBlank(message = "请输入搜索关键词") String keyword,
            @AuthenticationPrincipal UserPrincipal principal) {
        return Result.ok(userService.search(keyword, principal.id()));
    }
}
