package com.blog.config;

import com.blog.article.Article;
import com.blog.article.ArticleMapper;
import com.blog.article.ArticleTag;
import com.blog.article.ArticleTagMapper;
import com.blog.article.MarkdownRenderer;
import com.blog.common.util.TextUtils;
import com.blog.user.User;
import com.blog.user.UserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Order(10)
public class DemoDataInitializer implements ApplicationRunner {

    private final BlogProperties properties;
    private final UserService userService;
    private final ArticleMapper articleMapper;
    private final ArticleTagMapper tagMapper;
    private final MarkdownRenderer renderer;

    public DemoDataInitializer(BlogProperties properties, UserService userService,
                               ArticleMapper articleMapper, ArticleTagMapper tagMapper,
                               MarkdownRenderer renderer) {
        this.properties = properties;
        this.userService = userService;
        this.articleMapper = articleMapper;
        this.tagMapper = tagMapper;
        this.renderer = renderer;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!properties.demoData()) {
            return;
        }
        User admin = userService.findAdmin();
        if (admin == null) {
            return;
        }
        if (articleMapper.countAll() > 0) {
            return;
        }
        create(admin, "把博客做成一座可漫步的花园", "写作环境、内容架构与一次克制的重构。",
                List.of("随笔", "设计"), 9,
                """
                写作不是往容器里灌水，而是修一条可以反复走的路。

                ## 为什么重新开始

                我曾经拥有过三个博客：一个死于主题折腾，一个死于部署复杂，最后一个死于「等我把系统写完」。

                这次我给自己定了三条规则：

                1. **先写，再优化。** 内容比功能重要，阅读比配置重要。
                2. **格式为表达服务。** Markdown 是底稿，但表格、代码和图表应该自然长出来。
                3. **允许不完美。** 先上线，再迭代。

                > 一个可以持续写下去的系统，才是好系统。

                ## 这一期做了什么

                | 能力 | 说明 |
                | --- | --- |
                | 文章 | Markdown / 表格 / 代码块 / Mermaid |
                | 互动 | 点赞、评论、@ 回复 |
                | 空间 | 作者主页与草稿管理 |

                接下来的事情，交给时间。
                """);
        create(admin, "Mermaid 让技术写作长出手脚", "用流程图、时序图与状态图解释复杂系统。",
                List.of("Mermaid", "写作"), 26,
                """
                好的技术文章不只解释「是什么」，还要让读者看见「如何流动」。

                ## 一个发布流程

                ```mermaid
                flowchart LR
                    A[写作] --> B[预览]
                    B --> C{检查}
                    C -- 通过 --> D[发布]
                    C -- 修改 --> A
                    D --> E[读者互动]
                    E --> A
                ```

                Mermaid 使用文本描述图形，因此它可以被版本控制、被 diff，也可以被未来的自己读懂。

                ## 时序图也可以很轻

                ```mermaid
                sequenceDiagram
                    participant U as 读者
                    participant W as Web
                    participant A as API
                    U->>W: 打开文章
                    W->>A: GET /api/articles/1
                    A-->>W: HTML + TOC
                    W-->>U: 可阅读的页面
                ```

                图表渲染失败时，页面会保留源码并提示，而不是让整篇文章变成白屏。这是写作系统应有的温柔。
                """);
        create(admin, "在 Spring Boot 里搭一条内容流水线", "从 Markdown 到安全 HTML，再到 SEO 与阅读体验。",
                List.of("Spring Boot", "Markdown", "工程"), 3,
                """
                内容系统的核心不是编辑器，而是**一条可信的渲染流水线**。

                ## 从源文本到页面

                ```java
                Node document = parser.parse(markdown);
                String html = renderer.render(document);
                String safe = policy.sanitize(html);
                ```

                这条链路解决了三个问题：

                - Markdown 语法与 GFM 扩展的一致性；
                - 原始 HTML 带来的 XSS 风险；
                - 服务端渲染与 SEO 的内容可达性。

                ## 表格只是起点

                | 格式 | 处理方式 |
                | --- | --- |
                | 表格 | GFM 表格扩展 + 响应式容器 |
                | 代码 | 语言类名 + 前端按需高亮 |
                | 图片 | 懒加载 / 防盗链策略 / Lightbox |
                | 公式 | KaTeX 前端渲染 |
                | 图表 | Mermaid 懒渲染 + 失败降级 |

                ## 最后

                技术选择的价值不在于「新」，而在于它是否让下一次写作更轻松。
                """);
    }

    private void create(User admin, String title, String summary, List<String> tags,
                        int daysAgo, String markdown) {
        MarkdownRenderer.RenderedDocument rendered = renderer.render(markdown);
        Article article = new Article();
        article.setAuthorId(admin.getId());
        article.setTitle(title);
        article.setSummary(summary);
        article.setContentMd(markdown);
        article.setContentHtml(rendered.html());
        article.setStatus(1);
        article.setViewCount(18 + daysAgo * 3);
        article.setLikeCount(4 + daysAgo / 3);
        article.setCommentCount(0);
        article.setReadingMinutes(TextUtils.readingMinutes(rendered.plainText()));
        article.setPublishTime(LocalDateTime.now().minusDays(daysAgo));
        article.setCreatedAt(LocalDateTime.now().minusDays(daysAgo + 1));
        article.setUpdatedAt(LocalDateTime.now().minusDays(daysAgo));
        article.setDeleted(0);
        articleMapper.insert(article);
        for (String tag : tags) {
            ArticleTag row = new ArticleTag();
            row.setArticleId(article.getId());
            row.setTagName(tag);
            row.setCreatedAt(LocalDateTime.now().minusDays(daysAgo));
            tagMapper.insert(row);
        }
    }
}
