package com.blog.article;

import com.blog.article.dto.ArticleDtos;

/**
 * 站点公开信息聚合接口。
 *
 * <p>组合站点配置、博主资料和公开统计，供首页与页头使用。
 * 具体实现见 {@code com.blog.article.impl.SiteServiceImpl}。</p>
 *
 * @since 1.0.0
 */
public interface SiteService {

    /**
     * 获取站点聚合信息。
     *
     * @return 站点标题、简介、地址、博主摘要与统计
     */
    ArticleDtos.SiteView getSite();
}
