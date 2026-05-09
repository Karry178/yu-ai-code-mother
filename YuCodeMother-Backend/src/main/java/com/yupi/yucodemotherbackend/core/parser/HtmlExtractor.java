package com.yupi.yucodemotherbackend.core.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML 提取器 - 从单文件 HTML 中提取 CSS 和 JS
 */
public class HtmlExtractor {

    private static final Pattern STYLE_PATTERN = Pattern.compile("<style[^>]*>([\\s\\S]*?)</style>", Pattern.CASE_INSENSITIVE);
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("<script[^>]*>([\\s\\S]*?)</script>", Pattern.CASE_INSENSITIVE);

    /**
     * 从单文件 HTML 中提取 CSS
     */
    public static String extractCss(String html) {
        Matcher matcher = STYLE_PATTERN.matcher(html);
        StringBuilder css = new StringBuilder();
        while (matcher.find()) {
            css.append(matcher.group(1)).append("\n\n");
        }
        return css.toString().trim();
    }

    /**
     * 从单文件 HTML 中提取 JavaScript
     */
    public static String extractJs(String html) {
        Matcher matcher = SCRIPT_PATTERN.matcher(html);
        StringBuilder js = new StringBuilder();
        while (matcher.find()) {
            String scriptContent = matcher.group(1);
            // 跳过外部引用的 script 标签
            if (!scriptContent.trim().isEmpty()) {
                js.append(scriptContent).append("\n\n");
            }
        }
        return js.toString().trim();
    }

    /**
     * 移除 HTML 中的 style 和 script 标签，并添加外部引用
     */
    public static String cleanHtml(String html) {
        // 移除 style 标签
        String cleaned = html.replaceAll("<style[^>]*>[\\s\\S]*?</style>", "");
        // 移除 script 标签（保留外部引用）
        cleaned = cleaned.replaceAll("<script[^>]*>[\\s\\S]*?</script>", "");
        
        // 在 </head> 前添加 CSS 引用
        cleaned = cleaned.replaceFirst("</head>", "  <link rel=\"stylesheet\" href=\"style.css\">\n</head>");
        // 在 </body> 前添加 JS 引用
        cleaned = cleaned.replaceFirst("</body>", "  <script src=\"script.js\"></script>\n</body>");
        
        return cleaned;
    }
}
