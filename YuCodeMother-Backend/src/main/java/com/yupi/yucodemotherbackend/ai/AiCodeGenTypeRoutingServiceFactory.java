package com.yupi.yucodemotherbackend.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 代码生成类型路由服务工厂
 */
@Configuration
@Slf4j
public class AiCodeGenTypeRoutingServiceFactory {

	// 引入聊天大模型 ChatModel
	@Resource
	private ChatModel chatModel;

	/**
	 * 创建AI代码生成类型路由实例
	 */
	@Bean
	public AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService() {
		return AiServices.builder(AiCodeGenTypeRoutingService.class)
				.chatModel(chatModel)
				.build();
	}
}
