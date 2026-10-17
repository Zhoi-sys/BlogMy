package com.blog.common.util;

import com.blog.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Component
public class CursorCodec {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public String encode(LocalDateTime time, long id) {
        if (time == null) {
            throw new IllegalArgumentException("cursor time must not be null");
        }
        String raw = time.format(FORMATTER) + "|" + id;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

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

    public record Cursor(LocalDateTime time, long id) {
    }
}
