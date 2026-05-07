package com.yupi.yucodemotherbackend.core.parser;

/**
 * 代码解析器策略接口 ———— 对于方法参数不同的策略模式和模板方法模式，建议使用执行器模式（Executor）
 */
public interface CodeParser<T> {

	/**
	 * 解析代码内容
	 *
	 * @param codeContent 原始代码内容
	 * @return 解析后的结果对象
	 */
	T parseCode(String codeContent);
}
