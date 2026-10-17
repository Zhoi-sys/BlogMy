package com.blog.common.util;

import com.blog.common.api.PageResult;

import java.util.List;
import java.util.function.Function;

/**
 * 游标分页的统一裁剪逻辑：调用方多取一条判断 hasMore，
 * 避免每个列表接口重复编写 subList / nextCursor 代码。
 */
public final class PageResults {

    private PageResults() {
    }

    public static <T, R> PageResult<R> slice(List<T> fetchedRows, int pageSize,
                                             Function<T, String> nextCursor,
                                             Function<List<T>, List<R>> mapper) {
        boolean hasMore = fetchedRows.size() > pageSize;
        List<T> pageRows = hasMore ? fetchedRows.subList(0, pageSize) : fetchedRows;
        if (pageRows.isEmpty()) {
            return PageResult.empty();
        }
        String cursor = hasMore ? nextCursor.apply(pageRows.get(pageRows.size() - 1)) : null;
        return PageResult.of(mapper.apply(pageRows), cursor, hasMore);
    }
}
