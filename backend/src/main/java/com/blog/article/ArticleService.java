package com.blog.article;

import com.blog.article.dto.ArticleDtos;
import com.blog.common.api.PageResult;
import com.blog.security.UserPrincipal;

/**
 * 文章领域服务接口。
 *
 * <p>覆盖首页文章流、个人空间文章、后台管理、详情读取和文章生命周期。
 * 具体实现见 {@code com.blog.article.impl.ArticleServiceImpl}。</p>
 *
 * @since 1.0.0
 */
public interface ArticleService {

    /**
     * 查询首页已发布文章流。
     *
     * @param keyword 标题/摘要关键词，可为空
     * @param cursor  游标，首页传 {@code null}
     * @param size    每页条数
     * @return 按发布时间倒序的文章卡片分页
     */
    PageResult<ArticleDtos.ArticleCard> feed(String keyword, String cursor, int size);

    /**
     * 查询指定用户空间的文章。
     *
     * @param authorId  空间用户 ID
     * @param status    文章状态；为空时默认已发布
     * @param cursor    游标
     * @param size      每页条数
     * @param principal 当前用户；非本人只能查看已发布文章
     * @return 文章卡片游标分页
     */
    PageResult<ArticleDtos.ArticleCard> listByAuthor(Long authorId, Integer status, String cursor,
                                                     int size, UserPrincipal principal);

    /**
     * 查询博主后台文章列表（包含草稿与已下架文章）。
     *
     * @param status    可选状态筛选
     * @param cursor    游标
     * @param size      每页条数
     * @param principal 当前博主
     * @return 按最后更新时间倒序的文章卡片分页
     */
    PageResult<ArticleDtos.ArticleCard> manage(Integer status, String cursor, int size,
                                               UserPrincipal principal);

    /**
     * 查询文章详情，并按 IP 对公开阅读量做去重。
     *
     * @param id        文章 ID
     * @param principal 当前用户，可为空
     * @param ip        客户端 IP
     * @return 包含安全 HTML、目录、点赞状态和编辑权限的文章详情
     */
    ArticleDtos.ArticleDetail detail(Long id, UserPrincipal principal, String ip);

    /**
     * 新建文章。
     *
     * @param request   文章内容与状态
     * @param principal 当前博主
     * @return 新文章 ID 与状态
     */
    ArticleDtos.ArticleRef create(ArticleDtos.ArticleRequest request, UserPrincipal principal);

    /**
     * 编辑文章内容与元信息，不改变发布状态。
     *
     * @param id        文章 ID
     * @param request   文章内容与标签
     * @param principal 当前博主
     * @return 文章 ID 与状态
     */
    ArticleDtos.ArticleRef update(Long id, ArticleDtos.ArticleRequest request, UserPrincipal principal);

    /**
     * 执行发布、下架或软删除状态流转。
     *
     * @param id        文章 ID
     * @param action    {@code publish}、{@code offline} 或 {@code delete}
     * @param principal 当前博主
     * @return 文章 ID 与流转后的状态
     */
    ArticleDtos.ArticleRef changeStatus(Long id, String action, UserPrincipal principal);

    /**
     * 统计全站已发布文章的总点赞数。
     *
     * @return 总点赞数
     */
    long totalPublishedLikes();
}
