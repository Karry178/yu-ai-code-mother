package com.yupi.yucodemotherbackend.langgraph4j.ai;

import com.yupi.yucodemotherbackend.langgraph4j.model.ImageCollectionPlan;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 图片收集规划服务
 */
public interface ImageCollectionPlanService {

	/**
	 * 根据用户提示词分析需要收集的图片类型和参数
	 *
	 * @param userPrompt 用户提示词
	 * @return
	 */
	@SystemMessage(fromResource = "prompt/image-collection-plan-system-prompt.txt")
	ImageCollectionPlan planImageCollection(@UserMessage String userPrompt);
}
