package com.blog.article;

import com.blog.article.dto.ArticleDtos;
import com.blog.common.api.PageResult;
import com.blog.security.UserPrincipal;

/**
 * 文章领域服务接口。实现见 {@code com.blog.article.impl.ArticleServiceImpl}。
 */
public interface ArticleService {

    PageResult<ArticleDtos.ArticleCard> feed(String keyword, String cursor, int size);

    PageResult<ArticleDtos.ArticleCard> listByAuthor(Long authorId, Integer status, String cursor,
                                                     int size, UserPrincipal principal);

    PageResult<ArticleDtos.ArticleCard> manage(Integer status, String cursor, int size,
                                               UserPrincipal principal);

    ArticleDtos.ArticleDetail detail(Long id, UserPrincipal principal, String ip);

    ArticleDtos.ArticleRef create(ArticleDtos.ArticleRequest request, UserPrincipal principal);

    ArticleDtos.ArticleRef update(Long id, ArticleDtos.ArticleRequest request, UserPrincipal principal);

    ArticleDtos.ArticleRef changeStatus(Long id, String action, UserPrincipal principal);

    long totalPublishedLikes();
}
