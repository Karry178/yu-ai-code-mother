package com.yupi.yucodemotherbackend.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 引入推理流式模型
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.chat-model")
@Data
public class ReasoningStreamingChatModelConfig {

	private String baseUrl;

	private String apiKey;

	/**
	 * 推理流式模型（用于Vue项目生成，有工具调用能力）
	 * @return
	 */
	@Bean
	public StreamingChatModel reasoningStreamingChatModel() {

		// 快速对话模式：
		final String modelName = "deepseek-chat";
		final int maxTokens = 8192;

		// 推理模型模式：
		/*// 定义 DeepSeek 的推理模型
		final String modelName = "deepseek-reasoner";
		// 定义最大消耗Token数
		final int maxTokens = 32768;*/

		// 构造AI输出
		return OpenAiStreamingChatModel.builder()
				.apiKey(apiKey)
				.baseUrl(baseUrl)
				.modelName(modelName)
				.maxTokens(maxTokens)
				.logRequests(true)
				.logResponses(true)
				.build();
	}
}
