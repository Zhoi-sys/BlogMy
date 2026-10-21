package com.blog.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 用户数据访问接口。
 *
 * <p>所有 SQL 均在 {@code resources/mapper/UserMapper.xml} 中维护，
 * 本接口只声明参数契约。</p>
 *
 * @since 1.0.0
 */
@Mapper
public interface UserMapper {

    /**
     * 按主键查询用户。
     *
     * @param id 用户 ID
     * @return 用户实体；不存在时返回 {@code null}
     */
    User selectById(@Param("id") Long id);

    /**
     * 按用户名查询用户，包含密码哈希。
     *
     * @param username 用户名
     * @return 用户实体；不存在时返回 {@code null}
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 查询首个启用状态的博主账号。
     *
     * @return 博主用户；不存在时返回 {@code null}
     */
    User selectFirstAdmin();

    /**
     * 按用户名或昵称模糊搜索用户。
     *
     * @param keyword       搜索关键词
     * @param excludeUserId 排除的用户 ID，可为空
     * @param limit         最大返回条数
     * @return 用户列表
     */
    List<User> search(@Param("keyword") String keyword,
                      @Param("excludeUserId") Long excludeUserId,
                      @Param("limit") int limit);

    /**
     * 按 ID 批量查询用户。
     *
     * @param ids 用户 ID 集合，不能为空
     * @return 用户列表
     */
    List<User> selectByIds(@Param("ids") Collection<Long> ids);

    /**
     * 新增用户并回填自增主键。
     *
     * @param user 用户实体，必须包含用户名、密码、昵称、角色、状态和时间戳
     * @return 受影响行数
     */
    int insert(User user);

    /**
     * 更新用户昵称与简介。
     *
     * @param id        用户 ID
     * @param nickname  新昵称
     * @param bio       新简介
     * @param updatedAt 更新时间
     * @return 受影响行数
     */
    int updateProfile(@Param("id") Long id,
                      @Param("nickname") String nickname,
                      @Param("bio") String bio,
                      @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 更新用户角色，主要用于启动时提升配置的博主账号。
     *
     * @param id        用户 ID
     * @param role      新角色值
     * @param updatedAt 更新时间
     * @return 受影响行数
     */
    int updateRole(@Param("id") Long id,
                   @Param("role") int role,
                   @Param("updatedAt") LocalDateTime updatedAt);
}
