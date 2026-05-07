package com.yupi.yucodemotherbackend.core.parser;

import com.yupi.yucodemotherbackend.exception.BusinessException;
import com.yupi.yucodemotherbackend.exception.ErrorCode;
import com.yupi.yucodemotherbackend.model.enums.CodeGenTypeEnum;

/**
 * 代码解析执行器
 * 根据代码生成类型执行相应的解析逻辑
 */
public class CodeParserExecutor {

	// 创建对应的代码解析器并解析
	private static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();
	private static final MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();

	/**
	 * 根据不同的代码生成类型，执行代码解析执行器
	 *
	 * @param codeContent 代码内容
	 * @param codeGenTypeEnum 代码生成类型
	 * @return 解析结果(HtmlCodeResult 或 MultiFileCodeResult)
	 */
	public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenTypeEnum) {
		// switch模式，如果是HTML文件则创建一个对应的代码解析器并解析；多文件也同样操作
		return switch (codeGenTypeEnum) {
			case HTML -> htmlCodeParser.parseCode(codeContent);
			case MULTI_FILE -> multiFileCodeParser.parseCode(codeContent);
			default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
		};
	}
}
