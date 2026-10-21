package com.blog.common.store;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的键值存储实现。
 *
 * <p>非 local/test Profile 时启用，用于生产环境的令牌、黑名单、去重与限流。</p>
 *
 * @since 1.0.0
 */
@Component
@Profile("!local & !test")
public class RedisKeyValueStore implements KeyValueStore {

    private final StringRedisTemplate redis;

    /**
     * 创建 Redis 存储。
     *
     * @param redis Spring Redis 字符串模板
     */
    public RedisKeyValueStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(String key, String value, Duration ttl) {
        redis.opsForValue().set(key, value, ttl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String get(String key) {
        return redis.opsForValue().get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(String key) {
        redis.delete(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setIfAbsent(String key, String value, Duration ttl) {
        return Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(key, value, ttl));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long increment(String key, Duration ttl) {
        Long value = redis.opsForValue().increment(key);
        if (value != null && value == 1L) {
            redis.expire(key, ttl.toSeconds(), TimeUnit.SECONDS);
        }
        return value == null ? 0L : value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redis.hasKey(key));
    }
}
