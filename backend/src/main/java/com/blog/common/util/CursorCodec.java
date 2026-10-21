package com.blog.common.util;

import com.blog.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * 游标编码/解码器。
 *
 * <p>把列表排序所需的 {@code (时间, id)} 编码为 URL 安全的 Base64 字符串，
 * 避免把内部 SQL 排序字段直接暴露给客户端，同时防止客户端伪造时间格式。</p>
 *
 * @since 1.0.0
 */
@Component
public class CursorCodec {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * 编码游标。
     *
     * @param time 排序时间，不能为空
     * @param id   同时间下的唯一 ID
     * @return Base64URL 编码、无填充的游标
     * @throws IllegalArgumentException 时间为空时抛出
     */
    public String encode(LocalDateTime time, long id) {
        if (time == null) {
            throw new IllegalArgumentException("cursor time must not be null");
        }
        String raw = time.format(FORMATTER) + "|" + id;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 解码游标。
     *
     * @param cursor 游标字符串，可为空
     * @return 解码结果；空游标返回 {@code null} 表示第一页
     * @throws BusinessException 游标格式非法时抛出 400
     */
    public Cursor decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            int separator = raw.lastIndexOf('|');
            if (separator <= 0) {
                throw new IllegalArgumentException("bad cursor");
            }
            LocalDateTime time = LocalDateTime.parse(raw.substring(0, separator), FORMATTER);
            long id = Long.parseLong(raw.substring(separator + 1));
            return new Cursor(time, id);
        } catch (RuntimeException ex) {
            throw BusinessException.badRequest("无效的分页游标");
        }
    }

    /**
     * 游标内容。
     *
     * @param time 上一页最后一条的排序时间
     * @param id   上一页最后一条的 ID
     */
    public record Cursor(LocalDateTime time, long id) {
    }
}
