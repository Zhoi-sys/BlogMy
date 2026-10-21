package com.blog.common.store;

import java.time.Duration;

/**
 * 轻量键值存储抽象。
 *
 * <p>生产环境由 Redis 实现，本地与测试环境使用内存实现，使项目在无外部依赖时
 * 也能启动。用于 Refresh Token、JWT 黑名单、阅读去重和限流计数。</p>
 *
 * @since 1.0.0
 */
public interface KeyValueStore {

    /**
     * 写入键值并设置过期时间。
     *
     * @param key   键
     * @param value 值
     * @param ttl   存活时间
     */
    void set(String key, String value, Duration ttl);

    /**
     * 读取键值。
     *
     * @param key 键
     * @return 值；不存在或已过期时返回 {@code null}
     */
    String get(String key);

    /**
     * 删除键。
     *
     * @param key 键
     */
    void delete(String key);

    /**
     * 仅当键不存在时写入，用于 SETNX 语义。
     *
     * @param key   键
     * @param value 值
     * @param ttl   存活时间
     * @return 写入成功返回 {@code true}
     */
    boolean setIfAbsent(String key, String value, Duration ttl);

    /**
     * 原子自增，首次创建时设置过期时间。
     *
     * @param key 键
     * @param ttl 过期时间
     * @return 自增后的值
     */
    long increment(String key, Duration ttl);

    /**
     * 判断键是否存在且未过期。
     *
     * @param key 键
     * @return 存在返回 {@code true}
     */
    boolean exists(String key);
}
