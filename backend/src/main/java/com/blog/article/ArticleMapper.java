package com.blog.article;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 文章数据访问。分页使用游标而非 OFFSET，全部 SQL 见 ArticleMapper.xml。
 */
@Mapper
public interface ArticleMapper {

    Article selectById(@Param("id") Long id);

    List<Article> selectByIds(@Param("ids") Collection<Long> ids);

    List<Article> selectFeed(@Param("keyword") String keyword,
                             @Param("cursorTime") LocalDateTime cursorTime,
                             @Param("cursorId") Long cursorId,
                             @Param("limit") int limit);

    List<Article> selectPublishedByAuthor(@Param("authorId") Long authorId,
                                          @Param("cursorTime") LocalDateTime cursorTime,
                                          @Param("cursorId") Long cursorId,
                                          @Param("limit") int limit);

    List<Article> selectAuthorArticlesByStatus(@Param("authorId") Long authorId,
                                               @Param("status") int status,
                                               @Param("cursorTime") LocalDateTime cursorTime,
                                               @Param("cursorId") Long cursorId,
                                               @Param("limit") int limit);

    List<Article> selectManage(@Param("authorId") Long authorId,
                               @Param("status") Integer status,
                               @Param("cursorTime") LocalDateTime cursorTime,
                               @Param("cursorId") Long cursorId,
                               @Param("limit") int limit);

    long countAll();

    int insert(Article article);

    int update(Article article);

    int updateStatus(@Param("id") Long id,
                     @Param("status") int status,
                     @Param("publishTime") LocalDateTime publishTime,
                     @Param("updatedAt") LocalDateTime updatedAt);

    int softDelete(@Param("id") Long id, @Param("updatedAt") LocalDateTime updatedAt);

    int incrementViewCount(@Param("id") Long id);

    int incrementLikeCount(@Param("id") Long id);

    int decrementLikeCount(@Param("id") Long id);

    int incrementCommentCount(@Param("id") Long id);

    int decrementCommentCount(@Param("id") Long id);

    long countPublishedByAuthor(@Param("authorId") Long authorId);

    long sumPublishedLikesByAuthor(@Param("authorId") Long authorId);

    long sumPublishedViewsByAuthor(@Param("authorId") Long authorId);

    long sumPublishedCommentsByAuthor(@Param("authorId") Long authorId);

    long sumAllPublishedLikes();
}
