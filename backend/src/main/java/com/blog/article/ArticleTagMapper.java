package com.blog.article;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 文章标签数据访问接口，SQL 见 {@code resources/mapper/ArticleTagMapper.xml}。
 *
 * @since 1.0.0
 */
@Mapper
public interface ArticleTagMapper {

    /**
     * 批量查询文章标签，按主键升序。
     *
     * @param articleIds 文章 ID 集合，不能为空
     * @return 标签行列表
     */
    List<ArticleTag> selectByArticleIds(@Param("articleIds") Collection<Long> articleIds);

    /**
     * 删除某篇文章的全部标签。
     *
     * @param articleId 文章 ID
     * @return 受影响行数
     */
    int deleteByArticleId(@Param("articleId") Long articleId);

    /**
     * 新增文章标签并回填主键。
     *
     * @param articleTag 标签实体
     * @return 受影响行数
     */
    int insert(ArticleTag articleTag);
}
