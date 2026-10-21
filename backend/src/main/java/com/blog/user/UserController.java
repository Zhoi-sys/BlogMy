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

/**
 * 用户与个人空间接口。
 *
 * <p>公开接口提供用户资料和已发布文章列表；资料修改、@ 联想搜索需要登录。
 * 非本人访问空间时只能查看已发布文章，博主本人可查看自己的草稿与已下架文章。</p>
 *
 * @since 1.0.0
 */
@Validated
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final ArticleService articleService;

    /**
     * 创建用户控制器。
     *
     * @param userService    用户服务
     * @param articleService 文章服务
     */
    public UserController(UserService userService, ArticleService articleService) {
        this.userService = userService;
        this.articleService = articleService;
    }

    /**
     * 查询用户空间资料与公开统计。
     *
     * @param id 用户 ID
     * @return 用户资料、文章数、获赞数、阅读数与评论数
     */
    @GetMapping("/{id}")
    public Result<UserDtos.UserProfile> profile(@PathVariable Long id) {
        return Result.ok(userService.getProfile(id));
    }

    /**
     * 分页查询用户空间文章。
     *
     * <p>访客与普通读者只能查询已发布文章（status=1）；博主本人可传
     * status=0 查看草稿、status=2 查看已下架文章。</p>
     *
     * @param id        空间用户 ID
     * @param status    文章状态；缺省为已发布
     * @param cursor    游标，首页不传
     * @param size      每页条数，1-30
     * @param principal 当前登录用户，可为空
     * @return 文章卡片游标分页
     */
    @GetMapping("/{id}/articles")
    public Result<PageResult<ArticleDtos.ArticleCard>> articles(
            @PathVariable Long id,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") @Min(1) @Max(30) int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        return Result.ok(articleService.listByAuthor(id, status, cursor, size, principal));
    }

    /**
     * 修改当前用户昵称与简介。
     *
     * @param principal 当前登录用户
     * @param request   昵称（2-32 字符）与简介（最多 255 字符）
     * @return 更新后的用户资料
     */
    @PutMapping("/me")
    public Result<UserDtos.UserView> updateMe(@AuthenticationPrincipal UserPrincipal principal,
                                              @Valid @RequestBody UserDtos.UpdateProfileRequest request) {
        return Result.ok(userService.updateProfile(principal, request));
    }

    /**
     * 按用户名或昵称搜索用户，用于评论区的 @ 联想。
     *
     * <p>最多返回 8 条，并排除当前用户。仅登录用户可调用。</p>
     *
     * @param keyword   搜索关键词，不能为空
     * @param principal 当前登录用户
     * @return 匹配用户的摘要列表
     */
    @GetMapping("/search")
    public Result<List<UserDtos.UserSummary>> search(
            @RequestParam @NotBlank(message = "请输入搜索关键词") String keyword,
            @AuthenticationPrincipal UserPrincipal principal) {
        return Result.ok(userService.search(keyword, principal.id()));
    }
}
