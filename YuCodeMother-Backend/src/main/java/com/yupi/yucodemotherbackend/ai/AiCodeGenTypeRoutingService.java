package com.yupi.yucodemotherbackend.ai;

import com.yupi.yucodemotherbackend.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.SystemMessage;

/**
 * AI代码生成类型 - 智能路由服务
 * 使用结构化输出直接返回枚举类型
 */
public interface AiCodeGenTypeRoutingService {

	/**
	 * 根据用户需求只能选择代码生成类型
	 *
	 * @param userPrompt 用户提示词
	 * @return 推荐的代码生成类型
	 */
	@SystemMessage(fromResource = "prompt/codegen-routing-system-prompt.txt")
	CodeGenTypeEnum routeCodeGenType(String userPrompt);
}
