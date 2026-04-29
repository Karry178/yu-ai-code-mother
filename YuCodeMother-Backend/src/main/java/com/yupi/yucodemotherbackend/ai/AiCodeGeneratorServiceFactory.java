package com.yupi.yucodemotherbackend.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI服务创建工厂 - 把AI功能工厂化，简化创建过程，创建对象
 */
@Configuration  // 定义为配置类
public class AiCodeGeneratorServiceFactory {

	@Resource
	private ChatModel chatModel;

	/**
	 * 快速创建AI代码生成器服务
	 *
	 * @return
	 */
	@Bean
	public AiCodeGeneratorService aiCodeGeneratorService() {
		return AiServices.create(AiCodeGeneratorService.class, chatModel);
	}
}
