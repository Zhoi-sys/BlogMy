package com.blog.common.util;

import com.blog.common.api.PageResult;

import java.util.List;
import java.util.function.Function;

/**
 * 游标分页的统一裁剪逻辑。
 *
 * <p>调用方通常多取一条记录判断 {@code hasMore}；本工具负责裁剪当前页、
 * 计算下一条游标并调用映射函数，避免每个列表接口重复编写
 * {@code subList} 与 {@code nextCursor} 代码。</p>
 *
 * @since 1.0.0
 */
public final class PageResults {

    private PageResults() {
    }

    /**
     * 将“多取一条”的查询结果转换为游标分页响应。
     *
     * @param fetchedRows 已按排序查询到的数据，通常为 pageSize + 1 条
     * @param pageSize    期望返回的条数
     * @param nextCursor  根据最后一条数据生成下一页游标的函数
     * @param mapper      将当前页数据转换为响应 DTO 的函数
     * @param <T>         查询结果类型
     * @param <R>         响应 DTO 类型
     * @return 游标分页结果
     */
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
