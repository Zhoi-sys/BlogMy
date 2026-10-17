package com.blog.article.impl;

import com.blog.article.SiteService;
import com.blog.article.dto.ArticleDtos;
import com.blog.common.exception.BusinessException;
import com.blog.config.BlogProperties;
import com.blog.user.User;
import com.blog.user.UserService;
import com.blog.user.UserViewMapper;
import com.blog.user.dto.UserDtos;
import org.springframework.stereotype.Service;

@Service
public class SiteServiceImpl implements SiteService {

    private final UserService userService;
    private final UserViewMapper userViewMapper;
    private final BlogProperties properties;

    public SiteServiceImpl(UserService userService, UserViewMapper userViewMapper, BlogProperties properties) {
        this.userService = userService;
        this.userViewMapper = userViewMapper;
        this.properties = properties;
    }

    @Override
    public ArticleDtos.SiteView getSite() {
        User owner = userService.findAdmin();
        if (owner == null) {
            throw BusinessException.notFound("站点尚未初始化");
        }
        UserDtos.UserProfile profile = userService.getProfile(owner.getId());
        return new ArticleDtos.SiteView(
                properties.site().title(),
                properties.site().description(),
                properties.site().url(),
                userViewMapper.toSummary(owner),
                profile.stats()
        );
    }
}
