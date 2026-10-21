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

/**
 * 首页文章流接口。
 *
 * <p>公开访问，只返回已发布文章，按发布时间倒序并使用游标分页。</p>
 *
 * @since 1.0.0
 */
@Validated
@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final ArticleService articleService;

    /**
     * 创建首页文章流控制器。
     *
     * @param articleService 文章服务
     */
    public FeedController(ArticleService articleService) {
        this.articleService = articleService;
    }

    /**
     * 查询首页文章流。
     *
     * @param keyword 标题/摘要关键词，可为空
     * @param cursor  游标，首页不传
     * @param size    每页条数，1-30
     * @return 文章卡片游标分页
     */
    @GetMapping
    public Result<PageResult<ArticleDtos.ArticleCard>> feed(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") @Min(1) @Max(30) int size) {
        return Result.ok(articleService.feed(keyword, cursor, size));
    }
}
