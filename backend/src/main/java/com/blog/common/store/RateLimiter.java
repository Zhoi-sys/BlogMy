package com.blog.common.store;

import com.blog.common.exception.BusinessException;
import com.blog.config.BlogProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 固定窗口限流器。
 *
 * <p>基于 {@link KeyValueStore#increment(String, Duration)} 计数。首次请求设置窗口过期，
 * 在窗口内超过阈值时抛出 429 业务异常。生产环境由 Redis 保证多实例共享计数。</p>
 *
 * @since 1.0.0
 */
@Component
public class RateLimiter {

    private final KeyValueStore store;
    private final BlogProperties properties;

    /**
     * 创建限流器，依赖由 Spring 注入。
     *
     * @param store      键值存储
     * @param properties 应用配置
     */
    public RateLimiter(KeyValueStore store, BlogProperties properties) {
        this.store = store;
        this.properties = properties;
    }

    /**
     * 检查指定操作是否超过限流阈值。
     *
     * @param scope    限流维度，例如 login-ip、comment-user
     * @param identity 维度标识，例如 IP、用户名或用户 ID
     * @param limit    窗口内允许的最大次数
     * @param window   时间窗口
     * @throws BusinessException 超过阈值时抛出 429
     */
    public void check(String scope, String identity, int limit, Duration window) {
        if (!properties.rateLimit().enabled()) {
            return;
        }
        long count = store.increment("rate:" + scope + ":" + identity, window);
        if (count > limit) {
            throw BusinessException.tooManyRequests("操作过于频繁，请稍后再试");
        }
    }
}
