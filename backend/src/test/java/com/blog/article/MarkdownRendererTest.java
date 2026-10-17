package com.blog.article;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownRendererTest {

    private final MarkdownRenderer renderer = new MarkdownRenderer();

    @Test
    void rendersGfmTableCodeMermaidAndSanitizesRawHtml() {
        String markdown = """
                ## 标题

                | 列一 | 列二 |
                | --- | --- |
                | A | B |

                ```java
                System.out.println("hello");
                ```

                ```mermaid
                flowchart LR
                  A --> B
                ```

                <script>alert('xss')</script>

                ![图片](https://example.com/photo.jpg)
                """;

        MarkdownRenderer.RenderedDocument result = renderer.render(markdown);

        assertThat(result.html()).contains("table-wrap", "language-java", "language-mermaid");
        assertThat(result.html()).contains("loading=\"lazy\"");
        assertThat(result.html()).doesNotContain("<script>");
        assertThat(result.toc()).extracting(MarkdownRenderer.TocEntry::text).contains("标题");
    }

    @Test
    void keepsMermaidSourceForClientSideRendering() {
        String markdown = """
                ## 流程

                ```mermaid
                sequenceDiagram
                  A->>B: Hello
                ```
                """;

        MarkdownRenderer.RenderedDocument result = renderer.render(markdown);

        assertThat(result.html()).contains("language-mermaid");
        assertThat(result.html()).contains("sequenceDiagram");
        assertThat(result.toc()).hasSize(1);
    }
}
