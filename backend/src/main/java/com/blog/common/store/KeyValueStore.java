package com.blog.common.store;

import java.time.Duration;

/**
 * Small Redis abstraction. Local/test environments use an in-memory implementation
 * so the project can run without external infrastructure.
 */
public interface KeyValueStore {

    void set(String key, String value, Duration ttl);

    String get(String key);

    void delete(String key);

    boolean setIfAbsent(String key, String value, Duration ttl);

    long increment(String key, Duration ttl);

    boolean exists(String key);
}
