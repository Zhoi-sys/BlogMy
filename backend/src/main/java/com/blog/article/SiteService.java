package com.blog.article;

import com.blog.article.dto.ArticleDtos;

/**
 * 站点公开信息聚合接口。实现见 {@code com.blog.article.impl.SiteServiceImpl}。
 */
public interface SiteService {

    ArticleDtos.SiteView getSite();
}
