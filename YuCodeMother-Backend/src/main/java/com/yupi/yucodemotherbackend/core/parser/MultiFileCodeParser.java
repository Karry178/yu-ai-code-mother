package com.yupi.yucodemotherbackend.core.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.yupi.yucodemotherbackend.ai.model.MultiFileCodeResult;

/**
 * 多文件代码解析器（HTML + CSS + JS）
 */
public class MultiFileCodeParser implements CodeParser<MultiFileCodeResult> {

	private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
	private static final Pattern CSS_CODE_PATTERN = Pattern.compile("```css\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
	private static final Pattern JS_CODE_PATTERN = Pattern.compile("```(?:js|javascript)\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);


	/**
	 * 解析多文件代码（HTML + CSS + JS）
	 *
	 * @param codeContent 原始代码内容
	 * @return
	 */
	@Override
	public MultiFileCodeResult parseCode(String codeContent) {

		// 定义一个新的解析多文件代码的方法
		MultiFileCodeResult result = new MultiFileCodeResult();

		// 调用提取代码方法，提取各类代码
		String htmlCode = extractCodeByPattern(codeContent, HTML_CODE_PATTERN);
		String cssCode = extractCodeByPattern(codeContent, CSS_CODE_PATTERN);
		String jsCode = extractCodeByPattern(codeContent, JS_CODE_PATTERN);

		// 🔍 添加调试日志
		System.out.println("=== MultiFileCodeParser 解析结果 ===");
		System.out.println("HTML 代码长度: " + (htmlCode != null ? htmlCode.length() : 0));
		System.out.println("CSS 代码长度: " + (cssCode != null ? cssCode.length() : 0));
		System.out.println("JS 代码长度: " + (jsCode != null ? jsCode.length() : 0));

		// 🔧 后处理：如果 CSS 或 JS 为空，尝试从 HTML 中提取
		if (htmlCode != null && (cssCode == null || cssCode.trim().isEmpty() || jsCode == null || jsCode.trim().isEmpty())) {
			System.out.println("检测到单文件 HTML，尝试提取 CSS 和 JS...");
			
			// 从 HTML 中提取 CSS 和 JS
			String extractedCss = HtmlExtractor.extractCss(htmlCode);
			String extractedJs = HtmlExtractor.extractJs(htmlCode);
			
			if (!extractedCss.isEmpty()) {
				cssCode = extractedCss;
				System.out.println("成功从 HTML 中提取 CSS，长度: " + cssCode.length());
			}
			
			if (!extractedJs.isEmpty()) {
				jsCode = extractedJs;
				System.out.println("成功从 HTML 中提取 JS，长度: " + jsCode.length());
			}
			
			// 清理 HTML，移除内联的 style 和 script，添加外部引用
			if (!extractedCss.isEmpty() || !extractedJs.isEmpty()) {
				htmlCode = HtmlExtractor.cleanHtml(htmlCode);
				System.out.println("已清理 HTML，移除内联代码");
			}
		}

		// 设置HTML代码、CSS、JS的代码
		if (htmlCode != null && !htmlCode.trim().isEmpty()) {
			result.setHtmlCode(htmlCode.trim());
		}
		if (cssCode != null && !cssCode.trim().isEmpty()) {
			result.setCssCode(cssCode.trim());
		}
		if (jsCode != null && !jsCode.trim().isEmpty()) {
			result.setJsCode(jsCode.trim());
		}

		return result;
	}


	/**
	 * 根据正则模式提取代码
	 *
	 * @param content 原始内容
	 * @param pattern 正则模式
	 * @return 提取的代码
	 */
	private String extractCodeByPattern(String content, Pattern pattern) {
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}
}
