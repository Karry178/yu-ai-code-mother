package com.yupi.yucodemotherbackend.ai;

import com.yupi.yucodemotherbackend.utils.SpringContextUtil;
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

	/**
	 * 创建AI代码生成类型路由实例
	 */
	public AiCodeGenTypeRoutingService createAiCodeGenTypeRoutingService() {
		ChatModel chatModel = SpringContextUtil.getBean("routingChatModelPrototype", ChatModel.class);
		return AiServices.builder(AiCodeGenTypeRoutingService.class)
				.chatModel(chatModel)
				.build();
	}


	/**
	 * 默认提供一个Bean
	 */
	@Bean
	public AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService() {
		return createAiCodeGenTypeRoutingService();
	}
}
