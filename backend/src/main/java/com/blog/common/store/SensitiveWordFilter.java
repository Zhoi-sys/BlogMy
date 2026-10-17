package com.blog.common.store;

import com.blog.common.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class SensitiveWordFilter {

    private List<String> words = List.of();

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

    public void validate(String content) {
        String normalized = content.toLowerCase();
        for (String word : words) {
            if (!word.isBlank() && normalized.contains(word.toLowerCase())) {
                throw BusinessException.badRequest("评论包含不适合发布的内容");
            }
        }
    }
}
