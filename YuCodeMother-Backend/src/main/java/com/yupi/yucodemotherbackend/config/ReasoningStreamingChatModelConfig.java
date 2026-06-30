package com.yupi.yucodemotherbackend.config;

import com.yupi.yucodemotherbackend.monitor.AiModelMonitorListener;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.List;

/**
 * 引入推理流式模型
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.reasoning-streaming-chat-model")
@Data
public class ReasoningStreamingChatModelConfig {

	// 【监听】引入 AI 大模型监听器
	@Resource
	private AiModelMonitorListener aiModelMonitorListener;

	private String baseUrl;

	private String apiKey;

	private String modelName;

	private Integer maxTokens;

	private Double temperature;

	private boolean logRequests = false;

	private boolean logResponses = false;

	/**
	 * 推理流式模型（用于Vue项目生成，有工具调用能力）
	 *
	 * @return
	 */
	@Bean
	@Scope("prototype")  // 多例模式
	public StreamingChatModel reasoningStreamingChatModelPrototype() {
		// 构造AI输出
		return OpenAiStreamingChatModel.builder()
				.apiKey(apiKey)
				.baseUrl(baseUrl)
				.modelName(modelName)
				.maxTokens(maxTokens)
				.temperature(temperature)
				.logRequests(logRequests)
				.logResponses(logResponses)
				// 【监听】引入大模型监听器 -> 接收的是数组，需要用List.of()方法
				.listeners(List.of(aiModelMonitorListener))
				.build();
	}
}
