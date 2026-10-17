package com.blog.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 用户数据访问。SQL 全部在 resources/mapper/UserMapper.xml 中维护。
 */
@Mapper
public interface UserMapper {

    User selectById(@Param("id") Long id);

    User selectByUsername(@Param("username") String username);

    User selectFirstAdmin();

    List<User> search(@Param("keyword") String keyword,
                      @Param("excludeUserId") Long excludeUserId,
                      @Param("limit") int limit);

    List<User> selectByIds(@Param("ids") Collection<Long> ids);

    int insert(User user);

    int updateProfile(@Param("id") Long id,
                      @Param("nickname") String nickname,
                      @Param("bio") String bio,
                      @Param("updatedAt") LocalDateTime updatedAt);

    int updateRole(@Param("id") Long id,
                   @Param("role") int role,
                   @Param("updatedAt") LocalDateTime updatedAt);
}
