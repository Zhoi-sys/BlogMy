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

/**
 * 文章接口。
 *
 * <p>详情接口向公开读者开放；文章管理、创建、编辑和状态流转仅限博主（ADMIN）。
 * 文章正文由服务端 Markdown 渲染并清洗，详情响应同时返回目录结构。</p>
 *
 * @since 1.0.0
 */
@Validated
@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    /**
     * 创建文章控制器。
     *
     * @param articleService 文章服务
     */
    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    /**
     * 查询文章详情。
     *
     * <p>公开读者只能读取已发布文章；作者可读取自己的草稿和已下架文章用于预览。
     * 已发布文章会根据 IP 做 1 小时阅读量去重。</p>
     *
     * @param id        文章 ID
     * @param principal 当前用户，可为空
     * @param request   用于提取客户端 IP
     * @return 文章详情、渲染 HTML、目录、点赞状态与编辑权限
     */
    @GetMapping("/{id}")
    public Result<ArticleDtos.ArticleDetail> detail(@PathVariable Long id,
                                                    @AuthenticationPrincipal UserPrincipal principal,
                                                    HttpServletRequest request) {
        return Result.ok(articleService.detail(id, principal, WebUtils.clientIp(request)));
    }

    /**
     * 博主后台文章列表。
     *
     * <p>按最后更新时间倒序，支持按状态筛选；仅 ADMIN 可访问。</p>
     *
     * @param status    0 草稿、1 已发布、2 已下架；缺省为全部
     * @param cursor    游标
     * @param size      每页条数，1-30
     * @param principal 当前博主
     * @return 文章卡片游标分页
     */
    @GetMapping("/manage")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<ArticleDtos.ArticleCard>> manage(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") @Min(1) @Max(30) int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        return Result.ok(articleService.manage(status, cursor, size, principal));
    }

    /**
     * 新建文章，可直接保存草稿或发布。
     *
     * @param request   标题、摘要、Markdown 正文、标签与状态
     * @param principal 当前博主
     * @return 新文章 ID 与当前状态
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ArticleDtos.ArticleRef> create(@Valid @RequestBody ArticleDtos.ArticleRequest request,
                                                 @AuthenticationPrincipal UserPrincipal principal) {
        return Result.ok(articleService.create(request, principal));
    }

    /**
     * 编辑文章内容与元信息。
     *
     * <p>不改变文章状态；已发布文章保存后立即更新线上内容。</p>
     *
     * @param id        文章 ID
     * @param request   标题、摘要、Markdown 正文与标签
     * @param principal 当前博主
     * @return 文章 ID 与状态
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ArticleDtos.ArticleRef> update(@PathVariable Long id,
                                                 @Valid @RequestBody ArticleDtos.ArticleRequest request,
                                                 @AuthenticationPrincipal UserPrincipal principal) {
        return Result.ok(articleService.update(id, request, principal));
    }

    /**
     * 执行文章状态流转：发布、下架或软删除。
     *
     * @param id        文章 ID
     * @param request   动作：publish、offline 或 delete
     * @param principal 当前博主
     * @return 文章 ID 与流转后的状态
     */
    @PostMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ArticleDtos.ArticleRef> status(@PathVariable Long id,
                                                 @Valid @RequestBody ArticleDtos.ArticleStatusRequest request,
                                                 @AuthenticationPrincipal UserPrincipal principal) {
        return Result.ok(articleService.changeStatus(id, request.action(), principal));
    }
}
