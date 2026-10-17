package com.blog.article;

import com.blog.article.dto.ArticleDtos;
import com.blog.common.api.PageResult;
import com.blog.common.api.Result;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final ArticleService articleService;

    public FeedController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public Result<PageResult<ArticleDtos.ArticleCard>> feed(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") @Min(1) @Max(30) int size) {
        return Result.ok(articleService.feed(keyword, cursor, size));
    }
}
