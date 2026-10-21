package com.blog.common.store;

import com.blog.common.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 基础敏感词过滤器。
 *
 * <p>启动时从 {@code classpath:sensitive-words.txt} 加载词库，忽略空行与
 * {@code #} 注释行；评论命中任意词时拒绝发布。当前为简单包含匹配，
 * 词库与规则可由运维替换。</p>
 *
 * @since 1.0.0
 */
@Component
public class SensitiveWordFilter {

    private List<String> words = List.of();

    /**
     * 加载敏感词文件；读取失败时退化为空词库，避免阻塞应用启动。
     */
    @PostConstruct
    void load() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("sensitive-words.txt").getInputStream(), StandardCharsets.UTF_8))) {
            words = reader.lines()
                    .map(String::strip)
                    .filter(line -> !line.isBlank() && !line.startsWith("#"))
                    .toList();
        } catch (Exception ex) {
            words = List.of();
        }
    }

    /**
     * 校验文本是否包含敏感词。
     *
     * @param content 待校验文本
     * @throws BusinessException 命中敏感词时抛出参数错误
     */
    public void validate(String content) {
        String normalized = content.toLowerCase();
        for (String word : words) {
            if (!word.isBlank() && normalized.contains(word.toLowerCase())) {
                throw BusinessException.badRequest("评论包含不适合发布的内容");
            }
        }
    }
}
