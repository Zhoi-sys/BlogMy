package com.blog.common.store;

import com.blog.common.exception.BusinessException;
import com.blog.config.BlogProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RateLimiter {

    private final KeyValueStore store;
    private final BlogProperties properties;

    public RateLimiter(KeyValueStore store, BlogProperties properties) {
        this.store = store;
        this.properties = properties;
    }

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
