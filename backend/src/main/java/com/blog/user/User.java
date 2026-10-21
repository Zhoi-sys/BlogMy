package com.blog.user;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 用户实体，对应 {@code users} 表。
 *
 * <p>密码只保存 BCrypt 哈希；角色 1 为读者、9 为博主/管理员。持久化映射与全部 SQL
 * 位于 {@code resources/mapper/UserMapper.xml}。</p>
 *
 * @since 1.0.0
 */
@Getter
@Setter
public class User {

    /** 用户主键。 */
    private Long id;

    /** 登录用户名，唯一且保存前统一转小写。 */
    private String username;

    /** BCrypt 密码哈希，禁止保存或返回明文密码。 */
    private String password;

    /** 展示昵称，可修改。 */
    private String nickname;

    /** 头像 URL；一期为空时前端使用文字头像。 */
    private String avatarUrl;

    /** 个人简介，最多 255 字符。 */
    private String bio;

    /** 角色：1 读者，9 博主/管理员。 */
    private Integer role;

    /** 账号状态：1 启用，0 禁用。 */
    private Integer status;

    /** 注册时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
