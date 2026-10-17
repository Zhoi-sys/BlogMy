package com.blog.article;

import com.blog.article.dto.ArticleDtos;
import com.blog.common.api.PageResult;
import com.blog.common.api.Result;
import com.blog.common.util.WebUtils;
import com.blog.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/{id}")
    public Result<ArticleDtos.ArticleDetail> detail(@PathVariable Long id,
                                                    @AuthenticationPrincipal UserPrincipal principal,
                                                    HttpServletRequest request) {
        return Result.ok(articleService.detail(id, principal, WebUtils.clientIp(request)));
    }

    @GetMapping("/manage")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<ArticleDtos.ArticleCard>> manage(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") @Min(1) @Max(30) int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        return Result.ok(articleService.manage(status, cursor, size, principal));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ArticleDtos.ArticleRef> create(@Valid @RequestBody ArticleDtos.ArticleRequest request,
                                                 @AuthenticationPrincipal UserPrincipal principal) {
        return Result.ok(articleService.create(request, principal));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ArticleDtos.ArticleRef> update(@PathVariable Long id,
                                                 @Valid @RequestBody ArticleDtos.ArticleRequest request,
                                                 @AuthenticationPrincipal UserPrincipal principal) {
        return Result.ok(articleService.update(id, request, principal));
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ArticleDtos.ArticleRef> status(@PathVariable Long id,
                                                 @Valid @RequestBody ArticleDtos.ArticleStatusRequest request,
                                                 @AuthenticationPrincipal UserPrincipal principal) {
        return Result.ok(articleService.changeStatus(id, request.action(), principal));
    }
}
