package com.blog.common.api;

import java.util.List;

public record PageResult<T>(List<T> list, String nextCursor, boolean hasMore) {

    public static <T> PageResult<T> of(List<T> list, String nextCursor, boolean hasMore) {
        return new PageResult<>(list, nextCursor, hasMore);
    }

    public static <T> PageResult<T> empty() {
        return new PageResult<>(List.of(), null, false);
    }
}
