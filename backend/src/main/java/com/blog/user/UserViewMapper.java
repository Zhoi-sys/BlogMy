package com.blog.user;

import com.blog.user.dto.UserDtos;
import org.springframework.stereotype.Component;

/**
 * 用户视图组装器。集中维护 User -> DTO 的字段映射，避免各 Service 重复拼装。
 */
@Component
public class UserViewMapper {

    public UserDtos.UserView toView(User user) {
        if (user == null) {
            return null;
        }
        return new UserDtos.UserView(
                user.getId(), user.getUsername(), user.getNickname(),
                user.getAvatarUrl(), user.getBio(), user.getRole(), user.getCreatedAt());
    }

    public UserDtos.UserSummary toSummary(User user) {
        if (user == null) {
            return null;
        }
        return new UserDtos.UserSummary(
                user.getId(), user.getUsername(), user.getNickname(),
                user.getAvatarUrl(), user.getRole());
    }
}
