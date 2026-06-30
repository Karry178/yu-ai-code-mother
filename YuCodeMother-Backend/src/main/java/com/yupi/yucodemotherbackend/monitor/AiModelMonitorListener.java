package com.yupi.yucodemotherbackend.monitor;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.output.TokenUsage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * AI 大模型监听器
 */
@Component
public class AiModelMonitorListener implements ChatModelListener {

	// 设置一个用于存储请求开始时间的Key
	public static final String REQUEST_START_TIME_KEY = "request_start_time";
	// 用于监控上下文传递 -> 因为请求、响应事件的触发不是同一个线程
	public static final String MONITOR_CONTEXT_KEY = "monitor_context";

	// 引入指标收集器 AiModelMetricsCollector
	@Resource
	private AiModelMetricsCollector aiModelMetricsCollector;

	@Override
	public void onRequest(ChatModelRequestContext requestContext) {
		// 获取当前时间戳，记录当前起始时间的 key:value
		requestContext.attributes().put(REQUEST_START_TIME_KEY, Instant.now());
		// 从监控上下文中获取信息
		MonitorContext monitorContext = MonitorContextHolder.getContext();
		String userId = monitorContext.getUserId();
		String appId = monitorContext.getAppId();
		// 请求和响应都需要监控上下文获取的信息，但是不同线程无法获取相同信息，所以需要把monitorContext的对象通过MonitorRequest传递给Response
		requestContext.attributes().put(MONITOR_CONTEXT_KEY, monitorContext);
		// 读取模型名称
		String modelName = requestContext.chatRequest().modelName();
		// 记录请求指标
		aiModelMetricsCollector.recordRequest(userId, appId, modelName, "started");

	}

	@Override
	public void onResponse(ChatModelResponseContext responseContext) {
		// 从属性中获取request提供的监控信息（由 onRequest 方法存储）
		Map<Object, Object> attributes = responseContext.attributes();
		// 从监控上下文获取信息 - 因为onRequest将监控信息给到MONITOR_CONTEXT_KEY了
		MonitorContext context = (MonitorContext) attributes.get(MONITOR_CONTEXT_KEY);
		String userId = context.getUserId();
		String appId = context.getAppId();
		// 获取模型名称
		String modelName = responseContext.chatResponse().modelName();
		// 记录成功请求
		aiModelMetricsCollector.recordRequest(userId, appId, modelName, "success");
		// 记录响应时间
		recordResponseTime(attributes, userId, appId, modelName);
		// 记录 Token 使用情况
		recordTokenUsage(responseContext, userId, appId, modelName);
	}


	@Override
	public void onError(ChatModelErrorContext errorContext) {
		// 从监控上下文获取信息
		MonitorContext context = MonitorContextHolder.getContext();
		String userId = context.getUserId();
		String appId = context.getAppId();
		// 获取模型名称和错误类型
		String modelName = errorContext.chatRequest().modelName();
		String errorMessage = errorContext.error().getMessage();
		// 记录失败请求
		aiModelMetricsCollector.recordRequest(userId, appId, modelName, "error");
		aiModelMetricsCollector.recordError(userId, appId, modelName, errorMessage);
		// 记录响应时间（即使是错误响应）
		Map<Object, Object> attributes = errorContext.attributes();
		recordResponseTime(attributes, userId, appId, modelName);
	}


	/**
	 * 记录响应时间
	 *
	 * @param attributes
	 * @param userId
	 * @param appId
	 * @param modelName
	 */
	private void recordResponseTime(Map<Object, Object> attributes, String userId, String appId, String modelName) {
		// 拿到请求开始时间
		Instant startTime = (Instant) attributes.get(REQUEST_START_TIME_KEY);
		// 拿到当前响应时间Instant.now()，求出差值
		Duration responseTime = Duration.between(startTime, Instant.now());
		// 最后调用指标收集器 -> 记录响应时间
		aiModelMetricsCollector.recordResponseTime(userId, appId, modelName, responseTime);
	}


	private void recordTokenUsage(ChatModelResponseContext responseContext, String userId, String appId, String modelName) {
		TokenUsage tokenUsage = responseContext.chatResponse().metadata().tokenUsage();
		if (tokenUsage != null) {
			// 依次记录输入消耗、输出消耗、总消耗
			aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "input", tokenUsage.inputTokenCount());
			aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "output", tokenUsage.outputTokenCount());
			aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "total", tokenUsage.totalTokenCount());
		}
	}
}
