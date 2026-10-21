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

/**
 * 站点公开信息聚合实现。
 *
 * <p>取首个启用状态的博主账号及其公开统计，并合并站点配置中的标题、简介与地址。</p>
 *
 * @since 1.0.0
 */
@Service
public class SiteServiceImpl implements SiteService {

    private final UserService userService;
    private final UserViewMapper userViewMapper;
    private final BlogProperties properties;

    /**
     * 创建站点服务实例，依赖由 Spring 注入。
     *
     * @param userService    用户服务
     * @param userViewMapper 用户视图映射器
     * @param properties     应用配置
     */
    public SiteServiceImpl(UserService userService, UserViewMapper userViewMapper, BlogProperties properties) {
        this.userService = userService;
        this.userViewMapper = userViewMapper;
        this.properties = properties;
    }

    /**
     * {@inheritDoc}
     */
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
