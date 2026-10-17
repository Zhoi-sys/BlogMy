package com.blog.common.store;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Profile({"local", "test"})
public class InMemoryKeyValueStore implements KeyValueStore {

    private record Entry(String value, Instant expiresAt) {
    }

    private final ConcurrentHashMap<String, Entry> values = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Instant> counterExpiry = new ConcurrentHashMap<>();

    @Override
    public void set(String key, String value, Duration ttl) {
        values.put(key, new Entry(value, expiresAt(ttl)));
    }

    @Override
    public String get(String key) {
        Entry entry = values.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.expiresAt().isBefore(Instant.now())) {
            values.remove(key, entry);
            return null;
        }
        return entry.value();
    }

    @Override
    public void delete(String key) {
        values.remove(key);
        counterExpiry.remove(key);
    }

    @Override
    public boolean setIfAbsent(String key, String value, Duration ttl) {
        Instant now = Instant.now();
        Entry candidate = new Entry(value, expiresAt(ttl));
        return values.compute(key, (k, current) -> {
            if (current == null || current.expiresAt().isBefore(now)) {
                return candidate;
            }
            return current;
        }) == candidate;
    }

    @Override
    public long increment(String key, Duration ttl) {
        Instant now = Instant.now();
        AtomicLong counter = new AtomicLong();
        values.compute(key, (k, current) -> {
            long next = 1;
            if (current != null && current.expiresAt().isAfter(now)) {
                next = Long.parseLong(current.value()) + 1;
            }
            counter.set(next);
            return new Entry(Long.toString(next), expiresAt(ttl));
        });
        counterExpiry.put(key, expiresAt(ttl));
        return counter.get();
    }

    @Override
    public boolean exists(String key) {
        return get(key) != null;
    }

    private Instant expiresAt(Duration ttl) {
        return Instant.now().plus(ttl);
    }
}
