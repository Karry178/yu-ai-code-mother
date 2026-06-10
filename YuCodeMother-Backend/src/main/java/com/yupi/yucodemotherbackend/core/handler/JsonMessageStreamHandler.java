package com.yupi.yucodemotherbackend.core.handler;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yupi.yucodemotherbackend.ai.model.message.*;
import com.yupi.yucodemotherbackend.constatnt.AppConstant;
import com.yupi.yucodemotherbackend.core.builder.VueProjectBuilder;
import com.yupi.yucodemotherbackend.model.entity.User;
import com.yupi.yucodemotherbackend.model.enums.ChatHistoryMessageTypeEnum;
import com.yupi.yucodemotherbackend.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashSet;
import java.util.Set;

/**
 JSON 消息流处理器
 处理VUE_PROJECT 类型的复杂流式响应，包含工具调用信息
 */
@Slf4j
@Component
public class JsonMessageStreamHandler {

	// 引入Vue项目部署方法
	@Resource
	private VueProjectBuilder vueProjectBuilder;

	/**
	 * 处理 TokenStream（VUE_PROJECT）
	 * -> 解析 JSON 消息并重组为完整的响应格式
	 *
	 * @param originFlux 原始流
	 * @param chatHistoryService 聊天历史服务
	 * @param appId 应用Id
	 * @param loginUser 登录用户
	 * @return 处理后的流
	 */
	public Flux<String> handle(Flux<String> originFlux, ChatHistoryService chatHistoryService, Long appId, User loginUser) {

		// 收集数据用于生成后端记忆格式
		StringBuilder chatHistoryStringBuilder = new StringBuilder();
		// 用于跟踪已经见过的工具Id，判断是否是第一次调用，非第一次调用则可以直接用
		Set<String> seenToolIds = new HashSet<>();
		return originFlux
				.map(chunk -> {
					// 解析每个JSON消息块
					return handleJsonMessageChunk(chunk, chatHistoryStringBuilder, seenToolIds);
				})
				.filter(StrUtil::isNotEmpty)  // 过滤空字符串
				.doOnComplete(() -> {
					// 流式响应完成后，添加AI消息到对话历史
					String aiResponse = chatHistoryStringBuilder.toString();
					chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
					// [部署补充]所有的流式响应完成后 -> 引入vueProjectBuilder的异步构建方法 -> 同时要构造出 projectPath
					String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
					vueProjectBuilder.buildProjectAsync(projectPath);
				})
				.doOnError(error -> {
					// 如果 AI 回复失败，也要记录错误消息
					String errorMessage = "AI 回复失败：" + error.getMessage();
					chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
				});
	}


	/**
	 * 【重要实现】解析并收集 TokenStream 数据
	 *
	 * @param chunk
	 * @param chatHistoryStringBuilder
	 * @param seenToolIds
	 * @return
	 */
	private String handleJsonMessageChunk(String chunk, StringBuilder chatHistoryStringBuilder, Set<String> seenToolIds) {
		// 解析JSON
			// 1.先将TokenStream上游处理成的JSON字符串chunk 还原为 Java对象（先还原成父类StreamMessage -> 目的是拿到Type后再分类，而非直接还原成基类）
		StreamMessage streamMessage = JSONUtil.toBean(chunk, StreamMessage.class);
		StreamMessageTypeEnum typeEnum = StreamMessageTypeEnum.getEnumByValue(streamMessage.getType());
			// 2.switch分类
		switch (typeEnum) {
			// AI 回复型消息：
			case AI_RESPONSE -> {
				// 如果是AI的回复 -> 转为aiMessage的消息即可 -> 然后取出data -> 交给对话历史字符串拼接器拼接
				AiResponseMessage aiMessage = JSONUtil.toBean(chunk, AiResponseMessage.class);
				String data = aiMessage.getData();
				// 直接拼接响应
				chatHistoryStringBuilder.append(data);
				// 返回给前端，实时输出！
				return data;
			}
			// 工具调用型消息：
			case TOOL_REQUEST -> {
				// 如果是工具调用 -> 获取工具的Id -> 判断是否为第一次使用该工具
				ToolRequestMessage toolRequestMessage = JSONUtil.toBean(chunk, ToolRequestMessage.class);
				String toolId = toolRequestMessage.getId();
				// 检查是否是第一次看到这个工具Id
				if (toolId != null && !seenToolIds.contains(toolId)) {
					// 第一次调用该工具，记录 Id 并完整返回工具信息
					seenToolIds.add(toolId);
					return "\n\n[选择工具] 写入文件\n\n";
				} else {
					// 不是第一次调用这个工具，直接返回空
					return "";
				}
			}
			// 工具调用完成后：
			case TOOL_EXECUTED -> {
				// 先解析出信息
				ToolExecutedMessage toolExecutedMessage = JSONUtil.toBean(chunk, ToolExecutedMessage.class);
				JSONObject jsonObject = JSONUtil.parseObj(toolExecutedMessage.getArguments());
				String relativeFilePath = jsonObject.getStr("relativeFilePath");
				String suffix = FileUtil.getSuffix(relativeFilePath);
				String content = jsonObject.getStr("content");
				String result = String.format("""
						【工具调用】写入文件 %s
						```%s
						%s
						```
						""", relativeFilePath, suffix, content);
				// 输出前端和要持久化的内容
				String output = String.format("\n\n%s\n\n", result);
				chatHistoryStringBuilder.append(output);
				return output;
			}
			// 否则
			default -> {
				log.error("不支持的信息类型：{}", typeEnum);
				return "";
			}
		}
	}
}
