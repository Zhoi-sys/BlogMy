package com.blog.interaction;

import com.blog.common.util.TextUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 评论内容渲染器。
 *
 * <p>评论以“纯文本 + 服务端生成的结构化 mention 标记”存储，标记格式为
 * {@code [@昵称](uid:123)}。客户端提交的原始标记会被转义，只有服务端可生成
 * mention，从而避免昵称/用户 ID 伪造。渲染时所有文本均做 HTML 转义。</p>
 *
 * @since 1.0.0
 */
@Component
public class CommentContentRenderer {

    /** 结构化 mention 标记：{@code [@昵称](uid:123)}。 */
    private static final Pattern MENTION = Pattern.compile("\\[@([^\\]]{1,64})]\\(uid:(\\d+)\\)");

    /**
     * 将存储内容渲染为安全 HTML。
     *
     * @param stored 存储的纯文本与 mention 标记
     * @return 转义后的 HTML，mention 转为站内用户链接
     */
    public String render(String stored) {
        if (stored == null || stored.isBlank()) {
            return "";
        }
        StringBuilder html = new StringBuilder(stored.length() + 32);
        Matcher matcher = MENTION.matcher(stored);
        int last = 0;
        while (matcher.find()) {
            html.append(escapeText(stored.substring(last, matcher.start())));
            String nickname = matcher.group(1);
            String userId = matcher.group(2);
            html.append("<a class=\"mention\" href=\"/u/").append(userId).append("\">@")
                    .append(HtmlUtils.htmlEscape(nickname)).append("</a>");
            last = matcher.end();
        }
        html.append(escapeText(stored.substring(last)));
        return html.toString();
    }

    /**
     * 将 mention 标记还原为可读纯文本。
     *
     * @param stored 存储内容
     * @return 纯文本
     */
    public String plainText(String stored) {
        if (stored == null) {
            return "";
        }
        return MENTION.matcher(stored).replaceAll(match -> "@" + match.group(1));
    }

    /**
     * 生成评论摘要，用于通知预览。
     *
     * @param stored    存储内容
     * @param maxLength 最大码点数
     * @return 折叠空白并截断后的摘要
     */
    public String preview(String stored, int maxLength) {
        return TextUtils.truncate(TextUtils.normalizeWhitespace(plainText(stored)), maxLength);
    }

    /**
     * 提取内容中出现的被提及用户 ID。
     *
     * @param stored 存储内容
     * @return 去重后的用户 ID 集合
     */
    public Set<Long> extractUserIds(String stored) {
        Set<Long> ids = new LinkedHashSet<>();
        if (stored == null) {
            return ids;
        }
        Matcher matcher = MENTION.matcher(stored);
        while (matcher.find()) {
            ids.add(Long.parseLong(matcher.group(2)));
        }
        return ids;
    }

    /**
     * 生成结构化 mention 标记。
     *
     * @param nickname 用户昵称，方括号会被替换为全角字符
     * @param userId   用户 ID
     * @return mention 标记
     */
    public String marker(String nickname, Long userId) {
        String safeNickname = nickname == null ? "" : nickname.replace("[", "［").replace("]", "］");
        return "[@" + safeNickname + "](uid:" + userId + ")";
    }

    /**
     * HTML 转义并保留换行。
     *
     * @param text 原始文本
     * @return 安全 HTML 文本
     */
    private String escapeText(String text) {
        return HtmlUtils.htmlEscape(text)
                .replace("\r\n", "\n")
                .replace("\n", "<br>");
    }
}
