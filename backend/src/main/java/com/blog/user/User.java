package com.blog.user;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 用户实体。持久化映射与全部 SQL 位于 resources/mapper/UserMapper.xml。
 */
@Getter
@Setter
public class User {

    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String avatarUrl;
    private String bio;
    private Integer role;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
