package com.yupi.yucodemotherbackend.core.parser;

import com.yupi.yucodemotherbackend.ai.model.MultiFileCodeResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

		// 设置HTML代码、CSS、JS的代码
		if (htmlCode != null && !htmlCode.trim().isEmpty()) {
			result.setHtmlCode(htmlCode.trim());
		}
		if (cssCode != null && !cssCode.trim().isEmpty()) {
			result.setHtmlCode(cssCode.trim());
		}
		if (jsCode != null && !jsCode.trim().isEmpty()) {
			result.setHtmlCode(jsCode.trim());
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
