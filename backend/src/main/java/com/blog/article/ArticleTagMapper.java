package com.blog.article;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ArticleTagMapper {

    List<ArticleTag> selectByArticleIds(@Param("articleIds") Collection<Long> articleIds);

    int deleteByArticleId(@Param("articleId") Long articleId);

    int insert(ArticleTag articleTag);
}
