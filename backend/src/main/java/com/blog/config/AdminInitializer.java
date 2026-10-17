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

@Component
@Order(0)
public class AdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserMapper userMapper;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final BlogProperties properties;

    public AdminInitializer(UserMapper userMapper, UserService userService,
                            PasswordEncoder passwordEncoder, BlogProperties properties) {
        this.userMapper = userMapper;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

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
