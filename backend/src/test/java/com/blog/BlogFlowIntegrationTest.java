package com.blog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BlogFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullBlogInteractionFlow() throws Exception {
        JsonNode admin = login("admin", "Admin@123456");
        String adminToken = admin.path("data").path("accessToken").asText();
        assertThat(adminToken).isNotBlank();

        JsonNode readerA = register("it_reader_a");
        JsonNode readerB = register("it_reader_b");
        String tokenA = readerA.path("data").path("accessToken").asText();
        String tokenB = readerB.path("data").path("accessToken").asText();
        long userB = readerB.path("data").path("user").path("id").asLong();
        String nicknameB = readerB.path("data").path("user").path("nickname").asText();

        String markdown = """
                # 验收文章

                | 名称 | 结果 |
                | --- | --- |
                | 表格 | 通过 |

                ```mermaid
                flowchart LR
                  A --> B
                ```

                <img src=x onerror=alert(1)>
                """;
        JsonNode created = json(mockMvc.perform(post("/api/articles")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "集成验收文章",
                                "summary", "验证发布、渲染与互动",
                                "contentMd", markdown,
                                "tags", new String[]{"测试", "Mermaid"},
                                "status", 1
                        ))))
                .andExpect(status().isOk())
                .andReturn());
        long articleId = created.path("data").path("id").asLong();
        assertThat(articleId).isPositive();

        JsonNode detail = json(mockMvc.perform(get("/api/articles/{id}", articleId)).andReturn());
        String html = detail.path("data").path("contentHtml").asText();
        assertThat(html).contains("table-wrap", "language-mermaid");
        assertThat(html).doesNotContain("onerror");

        String likeBody = objectMapper.writeValueAsString(Map.of("targetType", 1, "targetId", articleId));
        JsonNode firstLike = json(mockMvc.perform(put("/api/likes")
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON).content(likeBody))
                .andExpect(status().isOk()).andReturn());
        JsonNode secondLike = json(mockMvc.perform(put("/api/likes")
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON).content(likeBody))
                .andExpect(status().isOk()).andReturn());
        assertThat(firstLike.path("data").path("likeCount").asInt()).isEqualTo(1);
        assertThat(secondLike.path("data").path("likeCount").asInt()).isEqualTo(1);

        String mentionText = "你好 @" + nicknameB + "，一起看看。";
        JsonNode comment = json(mockMvc.perform(post("/api/comments")
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "articleId", articleId,
                                "content", mentionText,
                                "mentionUserIds", new Long[]{userB}
                        ))))
                .andExpect(status().isOk()).andReturn());
        long rootId = comment.path("data").path("id").asLong();
        assertThat(comment.path("data").path("contentHtml").asText()).contains("mention");

        JsonNode reply = json(mockMvc.perform(post("/api/comments")
                        .header("Authorization", bearer(tokenB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "articleId", articleId,
                                "content", "收到，补充一点。",
                                "parentId", rootId
                        ))))
                .andExpect(status().isOk()).andReturn());
        assertThat(reply.path("data").path("replyToNickname").asText()).isNotBlank();

        JsonNode roots = json(mockMvc.perform(get("/api/comments")
                        .param("articleId", Long.toString(articleId))
                        .param("size", "10"))
                .andExpect(status().isOk()).andReturn());
        assertThat(roots.path("data").path("list").get(0).path("replyCount").asInt()).isEqualTo(1);

        JsonNode notificationsA = json(mockMvc.perform(get("/api/notifications")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk()).andReturn());
        JsonNode notificationsB = json(mockMvc.perform(get("/api/notifications")
                        .header("Authorization", bearer(tokenB)))
                .andExpect(status().isOk()).andReturn());
        assertThat(notificationsA.path("data").path("list").get(0).path("type").asInt()).isEqualTo(1);
        assertThat(notificationsB.path("data").path("list").get(0).path("type").asInt()).isEqualTo(2);

        JsonNode refreshed = json(mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "refreshToken", readerA.path("data").path("refreshToken").asText()))))
                .andExpect(status().isOk()).andReturn());
        assertThat(refreshed.path("data").path("accessToken").asText()).isNotBlank();

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "refreshToken", readerA.path("data").path("refreshToken").asText()))))
                .andExpect(status().isUnauthorized());
    }

    private JsonNode register(String username) throws Exception {
        return json(mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "password", "Demo12345"
                        ))))
                .andExpect(status().isOk())
                .andReturn());
    }

    private JsonNode login(String username, String password) throws Exception {
        return json(mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andReturn());
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
