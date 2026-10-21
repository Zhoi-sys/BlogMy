package com.blog.article;

import com.blog.common.util.TextUtils;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 服务端 Markdown 渲染与安全清洗管线。
 *
 * <p>使用 Flexmark 解析 GFM Markdown（表格、任务列表、脚注、自动链接等），
 * 转义原始 HTML，再通过 OWASP Java HTML Sanitizer 做元素/属性/协议白名单清洗。
 * 清洗后补充标题锚点、表格滚动容器、代码语言类名、图片懒加载和链接安全属性。
 * Mermaid 与 KaTeX 的源码会保留，由前端按需渲染。</p>
 *
 * @since 1.0.0
 */
@Component
public class MarkdownRenderer {

    private static final PolicyFactory POLICY = new HtmlPolicyBuilder()
            .allowElements(
                    "a", "p", "h1", "h2", "h3", "h4", "h5", "h6",
                    "ul", "ol", "li", "blockquote", "pre", "code", "hr", "br",
                    "strong", "b", "em", "i", "del", "s", "sup", "sub",
                    "table", "thead", "tbody", "tfoot", "tr", "th", "td",
                    "img", "div", "span", "section", "input", "label")
            .allowAttributes("href").onElements("a")
            .allowAttributes("src", "alt", "title", "width", "height").onElements("img")
            .allowAttributes("class").onElements(
                    "a", "code", "pre", "div", "span", "section", "ol", "ul", "li",
                    "table", "thead", "tbody", "tfoot", "tr", "th", "td")
            .allowAttributes("id").onElements("h1", "h2", "h3", "h4", "h5", "h6", "div", "li", "ol", "section", "a")
            .allowAttributes("colspan", "rowspan").onElements("th", "td")
            .allowAttributes("type", "checked", "disabled").onElements("input")
            .allowUrlProtocols("http", "https", "mailto")
            .toFactory();

    private static final Parser PARSER;
    private static final HtmlRenderer RENDERER;

    static {
        MutableDataSet options = new MutableDataSet();
        options.setFrom(ParserEmulationProfile.GITHUB);
        options.set(Parser.EXTENSIONS, List.of(
                TablesExtension.create(),
                StrikethroughExtension.create(),
                TaskListExtension.create(),
                AutolinkExtension.create(),
                FootnoteExtension.create(),
                TypographicExtension.create()
        ));
        options.set(HtmlRenderer.ESCAPE_HTML, true);
        options.set(HtmlRenderer.SUPPRESS_HTML, true);
        options.set(HtmlRenderer.GENERATE_HEADER_ID, false);
        options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");
        options.set(HtmlRenderer.FENCED_CODE_LANGUAGE_CLASS_PREFIX, "language-");
        PARSER = Parser.builder(options).build();
        RENDERER = HtmlRenderer.builder(options).build();
    }

    /**
     * 渲染 Markdown 为安全 HTML 与目录。
     *
     * @param markdown 原始 Markdown，可为空
     * @return 安全 HTML、纯文本与二级/三级标题目录
     */
    public RenderedDocument render(String markdown) {
        String source = markdown == null ? "" : markdown;
        Node node = PARSER.parse(source);
        String unsafeHtml = RENDERER.render(node);
        String sanitized = POLICY.sanitize(unsafeHtml);

        Document document = Jsoup.parseBodyFragment(sanitized);
        document.outputSettings()
                .prettyPrint(false)
                .escapeMode(Entities.EscapeMode.xhtml)
                .charset("UTF-8");

        Map<String, Integer> slugCounter = new HashMap<>();
        Set<String> usedIds = new HashSet<>();
        List<TocEntry> toc = new ArrayList<>();

        for (Element heading : document.select("h1, h2, h3, h4, h5, h6")) {
            String text = heading.text().strip();
            String base = TextUtils.slugify(text);
            int count = slugCounter.merge(base, 1, Integer::sum);
            String id = count == 1 ? base : base + "-" + count;
            while (!usedIds.add(id)) {
                count = slugCounter.merge(base, 1, Integer::sum);
                id = base + "-" + count;
            }
            heading.attr("id", id);
            int level = Integer.parseInt(heading.tagName().substring(1));
            if (level == 2 || level == 3) {
                toc.add(new TocEntry(level, id, text));
            }
        }

        for (Element table : document.select("table")) {
            table.addClass("md-table");
            table.wrap("<div class=\"table-wrap\"></div>");
        }

        for (Element pre : document.select("pre")) {
            pre.addClass("code-block");
            Element code = pre.selectFirst("code");
            if (code != null && code.classNames().stream().noneMatch(c -> c.startsWith("language-"))) {
                code.addClass("language-plaintext");
            }
        }

        for (Element link : document.select("a[href]")) {
            String href = link.attr("href").strip();
            if (href.startsWith("http://") || href.startsWith("https://")) {
                link.attr("target", "_blank");
                link.attr("rel", "noopener noreferrer nofollow");
            }
        }

        for (Element image : document.select("img[src]")) {
            image.attr("loading", "lazy");
            image.attr("decoding", "async");
            image.attr("referrerpolicy", "no-referrer");
            if (!image.hasAttr("alt")) {
                image.attr("alt", "");
            }
        }

        String html = document.body().html();
        String plainText = document.body().text();
        return new RenderedDocument(html, plainText, toc);
    }

    /**
     * 从已渲染 HTML 中提取二级/三级标题目录。
     *
     * @param html 已渲染 HTML
     * @return 目录项列表；空 HTML 返回空列表
     */
    public List<TocEntry> extractToc(String html) {
        if (html == null || html.isBlank()) {
            return List.of();
        }
        Document document = Jsoup.parseBodyFragment(html);
        List<TocEntry> toc = new ArrayList<>();
        for (Element heading : document.select("h2[id], h3[id]")) {
            int level = Integer.parseInt(heading.tagName().substring(1));
            toc.add(new TocEntry(level, heading.id(), heading.text().strip()));
        }
        return toc;
    }

    /**
     * Markdown 标题目录项。
     *
     * @param level 标题层级，2 或 3
     * @param id    标题锚点 ID
     * @param text  标题纯文本
     */
    public record TocEntry(int level, String id, String text) {
    }

    /**
     * Markdown 渲染结果。
     *
     * @param html      清洗后的安全 HTML
     * @param plainText 用于摘要与阅读时长的纯文本
     * @param toc       标题目录
     */
    public record RenderedDocument(String html, String plainText, List<TocEntry> toc) {
    }
}
