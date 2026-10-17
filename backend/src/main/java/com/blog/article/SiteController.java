package com.blog.article;

import com.blog.article.dto.ArticleDtos;
import com.blog.common.api.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/site")
public class SiteController {

    private final SiteService siteService;

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @GetMapping
    public Result<ArticleDtos.SiteView> site() {
        return Result.ok(siteService.getSite());
    }
}
