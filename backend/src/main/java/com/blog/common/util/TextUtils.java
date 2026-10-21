package com.blog.common.util;

import java.util.Locale;

/**
 * 文本处理工具。
 *
 * <p>所有方法均对 {@code null} 做安全处理，并按 Unicode 码点截断，
 * 避免把 emoji 等代理对字符截断成非法字符串。</p>
 *
 * @since 1.0.0
 */
public final class TextUtils {

    private TextUtils() {
    }

    /**
     * 按 Unicode 码点截断文本，超长时追加省略号。
     *
     * @param value     原始文本
     * @param maxLength 最大码点数
     * @return 截断后的文本；{@code null} 转为空字符串
     */
    public static String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String normalized = value.strip();
        if (normalized.codePointCount(0, normalized.length()) <= maxLength) {
            return normalized;
        }
        int end = normalized.offsetByCodePoints(0, maxLength);
        return normalized.substring(0, end).strip() + "…";
    }

    /**
     * 将连续空白折叠为单个空格并去除首尾空白。
     *
     * @param value 原始文本
     * @return 规范化文本；{@code null} 转为空字符串
     */
    public static String normalizeWhitespace(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").strip();
    }

    /**
     * 生成 URL 友好的标题锚点。
     *
     * @param value 标题文本
     * @return 小写、空白转连字符的 slug；无法生成时返回 {@code section}
     */
    public static String slugify(String value) {
        if (value == null || value.isBlank()) {
            return "section";
        }
        String slug = value.toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", "-")
                .replaceAll("^-+|-+$", "");
        return slug.isBlank() ? "section" : slug;
    }

    /**
     * 估算阅读时长。
     *
     * <p>中文按 1 字、英文按 2 个字符折算，约 400 单位/分钟，最少返回 1 分钟。</p>
     *
     * @param plainText 纯文本正文
     * @return 阅读分钟数
     */
    public static int readingMinutes(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return 1;
        }
        int chinese = 0;
        int latinWords = 0;
        boolean inWord = false;
        for (int i = 0; i < plainText.length(); i++) {
            char c = plainText.charAt(i);
            if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) {
                chinese++;
                inWord = false;
            } else if (Character.isLetterOrDigit(c)) {
                if (!inWord) {
                    latinWords++;
                    inWord = true;
                }
            } else {
                inWord = false;
            }
        }
        int units = chinese + latinWords * 2;
        return Math.max(1, (int) Math.ceil(units / 400.0));
    }
}
