package com.blog.common.util;

import java.util.Locale;

public final class TextUtils {

    private TextUtils() {
    }

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

    public static String normalizeWhitespace(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").strip();
    }

    public static String slugify(String value) {
        if (value == null || value.isBlank()) {
            return "section";
        }
        String slug = value.toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", "-")
                .replaceAll("^-+|-+$", "");
        return slug.isBlank() ? "section" : slug;
    }

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
