package com.blog.user.impl;

import com.blog.article.ArticleMapper;
import com.blog.common.exception.BusinessException;
import com.blog.security.UserPrincipal;
import com.blog.user.User;
import com.blog.user.UserMapper;
import com.blog.user.UserService;
import com.blog.user.UserViewMapper;
import com.blog.user.dto.UserDtos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final ArticleMapper articleMapper;
    private final UserViewMapper userViewMapper;

    public UserServiceImpl(UserMapper userMapper, ArticleMapper articleMapper,
                           UserViewMapper userViewMapper) {
        this.userMapper = userMapper;
        this.articleMapper = articleMapper;
        this.userViewMapper = userViewMapper;
    }

    @Override
    public User requireActive(Long id) {
        User user = userMapper.selectById(id);
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw BusinessException.notFound("用户不存在");
        }
        return user;
    }

    @Override
    public User findByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public User findAdmin() {
        return userMapper.selectFirstAdmin();
    }

    @Override
    public UserDtos.UserProfile getProfile(Long id) {
        User user = requireActive(id);
        long articleCount = articleMapper.countPublishedByAuthor(id);
        long likeCount = articleMapper.sumPublishedLikesByAuthor(id);
        long viewCount = articleMapper.sumPublishedViewsByAuthor(id);
        long commentCount = articleMapper.sumPublishedCommentsByAuthor(id);
        return new UserDtos.UserProfile(
                userViewMapper.toView(user),
                new UserDtos.UserStats(articleCount, likeCount, viewCount, commentCount)
        );
    }

    @Override
    @Transactional
    public UserDtos.UserView updateProfile(UserPrincipal principal, UserDtos.UpdateProfileRequest request) {
        User user = requireActive(principal.id());
        if (request.nickname() != null) {
            String nickname = request.nickname().strip();
            if (nickname.length() < 2) {
                throw BusinessException.badRequest("昵称至少需要 2 个字符");
            }
            user.setNickname(nickname);
        }
        if (request.bio() != null) {
            user.setBio(request.bio().strip());
        }
        LocalDateTime now = LocalDateTime.now();
        user.setUpdatedAt(now);
        userMapper.updateProfile(user.getId(), user.getNickname(), user.getBio(), now);
        return userViewMapper.toView(user);
    }

    @Override
    public List<UserDtos.UserSummary> search(String keyword, Long excludeUserId) {
        String normalized = keyword == null ? "" : keyword.strip();
        if (normalized.isBlank()) {
            return List.of();
        }
        return userMapper.search(normalized, excludeUserId, 8).stream()
                .map(userViewMapper::toSummary)
                .toList();
    }
}
