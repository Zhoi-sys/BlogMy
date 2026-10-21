package com.blog.user;

import com.blog.security.UserPrincipal;
import com.blog.user.dto.UserDtos;

import java.util.List;

/**
 * 用户领域服务接口。
 *
 * <p>提供用户查询、空间资料、资料修改和 @ 联想搜索。
 * 具体实现见 {@code com.blog.user.impl.UserServiceImpl}。</p>
 *
 * @since 1.0.0
 */
public interface UserService {

    /**
     * 获取处于启用状态的用户，不存在或被禁用时抛出业务异常。
     *
     * @param id 用户 ID
     * @return 用户实体
     */
    User requireActive(Long id);

    /**
     * 按用户名查询用户，包含密码哈希，仅供认证流程使用。
     *
     * @param username 已规范化的用户名
     * @return 用户实体；不存在时返回 {@code null}
     */
    User findByUsername(String username);

    /**
     * 查询站点首个启用状态的博主账号。
     *
     * @return 博主用户；尚未初始化时返回 {@code null}
     */
    User findAdmin();

    /**
     * 查询用户空间资料与公开统计。
     *
     * @param id 用户 ID
     * @return 用户资料及文章、获赞、阅读、评论统计
     */
    UserDtos.UserProfile getProfile(Long id);

    /**
     * 修改当前用户昵称和简介。
     *
     * @param principal 当前登录用户
     * @param request   修改请求，字段为空表示保持不变
     * @return 更新后的用户资料
     */
    UserDtos.UserView updateProfile(UserPrincipal principal, UserDtos.UpdateProfileRequest request);

    /**
     * 按用户名或昵称搜索可被 @ 的用户。
     *
     * @param keyword       搜索关键词
     * @param excludeUserId 需要排除的用户 ID，通常为当前用户
     * @return 最多 8 个用户摘要
     */
    List<UserDtos.UserSummary> search(String keyword, Long excludeUserId);
}
