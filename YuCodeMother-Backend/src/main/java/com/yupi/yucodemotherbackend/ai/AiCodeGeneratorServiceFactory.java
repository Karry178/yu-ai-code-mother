package com.yupi.yucodemotherbackend.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI服务创建工厂 - 把AI功能工厂化，简化创建过程，创建对象
 */
@Configuration  // 定义为配置类
public class AiCodeGeneratorServiceFactory {

	// 引入普通的模型对话方式
	@Resource
	private ChatModel chatModel;

	// 引入流式输出模型对话方式
	@Resource
	private StreamingChatModel streamingChatModel;

	/**
	 * 快速创建AI代码生成器服务
	 *
	 * @return
	 */
	@Bean
	public AiCodeGeneratorService aiCodeGeneratorService() {
		return AiServices.builder(AiCodeGeneratorService.class)
				// 绑定各种方式的大模型对象
				.chatModel(chatModel)
				.streamingChatModel(streamingChatModel)
				.build();
	}
}
