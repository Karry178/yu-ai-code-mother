package com.yupi.yucodemotherbackend.langgraph4j.ai;

import com.yupi.yucodemotherbackend.langgraph4j.model.QualityResult;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 代码质量检查服务接口
 */
public interface CodeQualityCheckService {

	@SystemMessage(fromResource = "prompt/code-quality-check-system-prompt.txt")
	QualityResult checkCodeQuality(@UserMessage String codeContent);
}
