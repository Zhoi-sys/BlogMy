package com.blog.article;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 文章数据访问接口。
 *
 * <p>分页使用 {@code (时间, id)} 游标而非 OFFSET；计数字段通过数据库原子更新维护。
 * 全部 SQL 见 {@code resources/mapper/ArticleMapper.xml}。</p>
 *
 * @since 1.0.0
 */
@Mapper
public interface ArticleMapper {

    /**
     * 按主键查询未删除文章。
     *
     * @param id 文章 ID
     * @return 文章实体；不存在或已删除时返回 {@code null}
     */
    Article selectById(@Param("id") Long id);

    /**
     * 按 ID 批量查询未删除文章。
     *
     * @param ids 文章 ID 集合，不能为空
     * @return 文章列表
     */
    List<Article> selectByIds(@Param("ids") Collection<Long> ids);

    /**
     * 查询首页已发布文章流。
     *
     * @param keyword    标题/摘要关键词，可为空
     * @param cursorTime 上一页最后一条的发布时间，首页为空
     * @param cursorId   上一页最后一条的文章 ID，首页为空
     * @param limit      查询条数，通常为 pageSize + 1
     * @return 按发布时间倒序的文章列表
     */
    List<Article> selectFeed(@Param("keyword") String keyword,
                             @Param("cursorTime") LocalDateTime cursorTime,
                             @Param("cursorId") Long cursorId,
                             @Param("limit") int limit);

    /**
     * 查询用户已发布文章。
     *
     * @param authorId   作者 ID
     * @param cursorTime 上一页最后一条的发布时间
     * @param cursorId   上一页最后一条的文章 ID
     * @param limit      查询条数
     * @return 已发布文章列表
     */
    List<Article> selectPublishedByAuthor(@Param("authorId") Long authorId,
                                          @Param("cursorTime") LocalDateTime cursorTime,
                                          @Param("cursorId") Long cursorId,
                                          @Param("limit") int limit);

    /**
     * 查询用户指定状态的草稿或已下架文章。
     *
     * @param authorId   作者 ID
     * @param status     文章状态
     * @param cursorTime 上一页最后一条的创建时间
     * @param cursorId   上一页最后一条的文章 ID
     * @param limit      查询条数
     * @return 按创建时间倒序的文章列表
     */
    List<Article> selectAuthorArticlesByStatus(@Param("authorId") Long authorId,
                                               @Param("status") int status,
                                               @Param("cursorTime") LocalDateTime cursorTime,
                                               @Param("cursorId") Long cursorId,
                                               @Param("limit") int limit);

    /**
     * 查询博主后台文章列表。
     *
     * @param authorId   作者 ID
     * @param status     可选状态；为 {@code null} 时查询全部
     * @param cursorTime 上一页最后一条的更新时间
     * @param cursorId   上一页最后一条的文章 ID
     * @param limit      查询条数
     * @return 按更新时间倒序的文章列表
     */
    List<Article> selectManage(@Param("authorId") Long authorId,
                               @Param("status") Integer status,
                               @Param("cursorTime") LocalDateTime cursorTime,
                               @Param("cursorId") Long cursorId,
                               @Param("limit") int limit);

    /**
     * 统计未删除文章总数，用于演示数据初始化判断。
     *
     * @return 文章数
     */
    long countAll();

    /**
     * 新增文章并回填自增主键。
     *
     * @param article 文章实体
     * @return 受影响行数
     */
    int insert(Article article);

    /**
     * 更新文章可编辑字段，不覆盖状态和计数器。
     *
     * @param article 包含最新标题、摘要、正文和标签信息的文章
     * @return 受影响行数
     */
    int update(Article article);

    /**
     * 更新文章发布状态与发布时间。
     *
     * @param id          文章 ID
     * @param status      新状态
     * @param publishTime 发布时间
     * @param updatedAt   更新时间
     * @return 受影响行数
     */
    int updateStatus(@Param("id") Long id,
                     @Param("status") int status,
                     @Param("publishTime") LocalDateTime publishTime,
                     @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 逻辑删除文章。
     *
     * @param id        文章 ID
     * @param updatedAt 更新时间
     * @return 受影响行数
     */
    int softDelete(@Param("id") Long id, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 阅读数原子加一。
     *
     * @param id 文章 ID
     * @return 受影响行数
     */
    int incrementViewCount(@Param("id") Long id);

    /**
     * 点赞数原子加一。
     *
     * @param id 文章 ID
     * @return 受影响行数
     */
    int incrementLikeCount(@Param("id") Long id);

    /**
     * 点赞数原子减一，最低为 0。
     *
     * @param id 文章 ID
     * @return 受影响行数
     */
    int decrementLikeCount(@Param("id") Long id);

    /**
     * 评论数原子加一。
     *
     * @param id 文章 ID
     * @return 受影响行数
     */
    int incrementCommentCount(@Param("id") Long id);

    /**
     * 评论数原子减一，最低为 0。
     *
     * @param id 文章 ID
     * @return 受影响行数
     */
    int decrementCommentCount(@Param("id") Long id);

    /**
     * 统计作者已发布文章数。
     *
     * @param authorId 作者 ID
     * @return 文章数
     */
    long countPublishedByAuthor(@Param("authorId") Long authorId);

    /**
     * 汇总作者已发布文章获赞数。
     *
     * @param authorId 作者 ID
     * @return 总获赞数
     */
    long sumPublishedLikesByAuthor(@Param("authorId") Long authorId);

    /**
     * 汇总作者已发布文章阅读数。
     *
     * @param authorId 作者 ID
     * @return 总阅读数
     */
    long sumPublishedViewsByAuthor(@Param("authorId") Long authorId);

    /**
     * 汇总作者已发布文章评论数。
     *
     * @param authorId 作者 ID
     * @return 总评论数
     */
    long sumPublishedCommentsByAuthor(@Param("authorId") Long authorId);

    /**
     * 统计全站已发布文章总点赞数。
     *
     * @return 总点赞数
     */
    long sumAllPublishedLikes();
}
