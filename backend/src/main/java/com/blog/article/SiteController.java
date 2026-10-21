package com.blog.article;

import com.blog.article.dto.ArticleDtos;
import com.blog.common.api.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 站点公开信息接口。
 *
 * <p>返回站点标题、简介、博主摘要与统计，用于首页 Hero 区与页头。</p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/site")
public class SiteController {

    private final SiteService siteService;

    /**
     * 创建站点信息控制器。
     *
     * @param siteService 站点聚合服务
     */
    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    /**
     * 获取站点聚合信息。
     *
     * @return 站点配置、博主摘要与文章/点赞/阅读统计
     */
    @GetMapping
    public Result<ArticleDtos.SiteView> site() {
        return Result.ok(siteService.getSite());
    }
}
