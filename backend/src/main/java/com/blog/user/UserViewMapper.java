package com.blog.user;

import com.blog.user.dto.UserDtos;
import org.springframework.stereotype.Component;

/**
 * 用户视图组装器。
 *
 * <p>集中维护 {@link User} 到用户 DTO 的字段映射，避免各业务 Service 重复拼装。</p>
 *
 * @since 1.0.0
 */
@Component
public class UserViewMapper {

    /**
     * 转换为用户完整视图。
     *
     * @param user 用户实体，可为空
     * @return 用户视图；入参为空时返回 {@code null}
     */
    public UserDtos.UserView toView(User user) {
        if (user == null) {
            return null;
        }
        return new UserDtos.UserView(
                user.getId(), user.getUsername(), user.getNickname(),
                user.getAvatarUrl(), user.getBio(), user.getRole(), user.getCreatedAt());
    }

    /**
     * 转换为用户摘要。
     *
     * @param user 用户实体，可为空
     * @return 用户摘要；入参为空时返回 {@code null}
     */
    public UserDtos.UserSummary toSummary(User user) {
        if (user == null) {
            return null;
        }
        return new UserDtos.UserSummary(
                user.getId(), user.getUsername(), user.getNickname(),
                user.getAvatarUrl(), user.getRole());
    }
}
