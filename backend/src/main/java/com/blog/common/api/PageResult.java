package com.blog.common.api;

import java.util.List;

/**
 * 游标分页响应。
 *
 * <p>不使用页码和 OFFSET，避免深分页性能下降。调用方把 {@code nextCursor}
 * 原样传回即可获取下一页；{@code hasMore=false} 时表示没有更多数据。</p>
 *
 * @param list       当前页数据
 * @param nextCursor 下一页游标；没有更多数据时为 {@code null}
 * @param hasMore    是否还有下一页
 * @param <T>        列表元素类型
 * @since 1.0.0
 */
public record PageResult<T>(List<T> list, String nextCursor, boolean hasMore) {

    /**
     * 构造游标分页结果。
     *
     * @param list       当前页数据
     * @param nextCursor 下一页游标
     * @param hasMore    是否还有更多
     * @param <T>        元素类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(List<T> list, String nextCursor, boolean hasMore) {
        return new PageResult<>(list, nextCursor, hasMore);
    }

    /**
     * 构造空分页结果。
     *
     * @param <T> 元素类型
     * @return 空分页
     */
    public static <T> PageResult<T> empty() {
        return new PageResult<>(List.of(), null, false);
    }
}
