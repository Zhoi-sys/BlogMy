package com.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 个人博客后端启动类。
 *
 * <p>启用配置属性扫描与定时任务；关闭 Spring Boot 默认的 UserDetails 自动配置，
 * 避免生成无用的默认安全密码。应用采用无状态 JWT 鉴权。</p>
 *
 * @since 1.0.0
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan
@EnableScheduling
public class BlogApplication {

    /**
     * 应用入口。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }
}
