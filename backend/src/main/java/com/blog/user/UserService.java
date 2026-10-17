package com.blog.user;

import com.blog.security.UserPrincipal;
import com.blog.user.dto.UserDtos;

import java.util.List;

/**
 * 用户领域服务接口。实现见 {@code com.blog.user.impl.UserServiceImpl}。
 */
public interface UserService {

    User requireActive(Long id);

    User findByUsername(String username);

    User findAdmin();

    UserDtos.UserProfile getProfile(Long id);

    UserDtos.UserView updateProfile(UserPrincipal principal, UserDtos.UpdateProfileRequest request);

    List<UserDtos.UserSummary> search(String keyword, Long excludeUserId);
}
