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
 * Server-side Markdown rendering pipeline.
 *
 * <p>Markdown is parsed with Flexmark (GFM tables, task lists, footnotes, autolinks),
 * raw HTML is escaped, the output is sanitized with OWASP Java HTML Sanitizer, and
 * safe presentation attributes (heading anchors, lazy images, code classes) are added
 * afterwards. Mermaid and KaTeX are hydrated by the frontend from the preserved source.</p>
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

    public record TocEntry(int level, String id, String text) {
    }

    public record RenderedDocument(String html, String plainText, List<TocEntry> toc) {
    }
}
