package com.blog.config;

import com.blog.user.User;
import com.blog.user.UserMapper;
import com.blog.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * 博主账号初始化器。
 *
 * <p>应用启动时根据 {@code blog.admin.*} 配置创建或提升管理员账号；
 * 在演示数据初始化之前执行（{@code @Order(0)}），保证演示文章有作者可挂载。
 * 已存在的博主账号不会被重置密码。</p>
 *
 * @since 1.0.0
 */
@Component
@Order(0)
public class AdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserMapper userMapper;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final BlogProperties properties;

    /**
     * 创建 {@code AdminInitializer} 实例，依赖由 Spring 注入。
     * @param userMapper 依赖组件
     * @param userService 依赖组件
     * @param passwordEncoder 依赖组件
     * @param properties 依赖组件
     */
    public AdminInitializer(UserMapper userMapper, UserService userService, PasswordEncoder passwordEncoder, BlogProperties properties) {
        this.userMapper = userMapper;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    /**
     * 创建或提升博主账号。
     *
     * @param args 启动参数
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        BlogProperties.Admin admin = properties.admin();
        if (admin == null || admin.username() == null || admin.username().isBlank()) {
            return;
        }
        String username = admin.username().strip().toLowerCase(Locale.ROOT);
        User existing = userService.findByUsername(username);
        if (existing != null) {
            if (existing.getRole() != 9) {
                userMapper.updateRole(existing.getId(), 9, java.time.LocalDateTime.now());
                log.info("Promoted existing user '{}' to administrator", username);
            }
            return;
        }
        if (admin.password() == null || admin.password().isBlank()) {
            log.warn("ADMIN_PASSWORD is not configured; administrator account was not created.");
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(admin.password()));
        user.setNickname(admin.nickname() == null || admin.nickname().isBlank() ? "站长" : admin.nickname().strip());
        user.setAvatarUrl("");
        user.setBio("写作、代码与长期主义。");
        user.setRole(9);
        user.setStatus(1);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);
        log.info("Administrator '{}' created from startup configuration", username);
    }
}
