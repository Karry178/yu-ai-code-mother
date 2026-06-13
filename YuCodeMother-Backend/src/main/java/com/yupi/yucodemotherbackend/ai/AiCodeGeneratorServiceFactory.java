package com.yupi.yucodemotherbackend.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yupi.yucodemotherbackend.ai.tools.*;
import com.yupi.yucodemotherbackend.exception.BusinessException;
import com.yupi.yucodemotherbackend.exception.ErrorCode;
import com.yupi.yucodemotherbackend.model.enums.CodeGenTypeEnum;
import com.yupi.yucodemotherbackend.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * AI服务创建工厂 - 把AI功能工厂化，简化创建过程，创建对象
 */
@Configuration  // 定义为配置类
@Slf4j
public class AiCodeGeneratorServiceFactory {

	// 引入普通的模型对话方式
	@Resource
	private ChatModel chatModel;

	// 引入流式输出模型对话方式
	@Resource
	private StreamingChatModel openAiStreamingChatModel;

	// 引入推理流式模型
	@Resource
	private StreamingChatModel reasoningStreamingChatModel;

	// 引入Redis记忆存储
	@Resource
	private RedisChatMemoryStore redisChatMemoryStore;

	// 引入ChatHistoryService，获取历史对话记忆
	@Resource
	private ChatHistoryService chatHistoryService;

	// 引入 ToolManager 工具管理器，拿到定义的所有文件操作工具
	@Resource
	private ToolManager toolManager;


	/**
	 * AI 服务实例缓存 - Caffeine本地缓存
	 * 缓存策略：
	 * - 最大缓存 1000 个实例
	 * - 写入后 30min 过期
	 * - 访问后 10min 过期
	 */
	private final Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
			.maximumSize(1000)
			.expireAfterWrite(Duration.ofMinutes(30))
			.expireAfterAccess(Duration.ofMinutes(10))
			.removalListener((key,  value, cause) -> {
					log.debug("AI 服务实例被移除，缓存键: {}, 原因：{}", key, cause);
			})
			.build();


	/**
	 * 缓存引用数据：有缓存直接取到，没缓存的话直接去创建再拿到
	 * @param appId
	 * @return
	 */
	public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
		return getAiCodeGeneratorService(appId, CodeGenTypeEnum.HTML);
	}


	/**
	 * 根据 appId 获取服务
	 * @param appId 应用Id
	 * @param codeGenType 代码类型
	 * @return
	 */
	public AiCodeGeneratorService getAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
		String cacheKey = buildCacheKey(appId, codeGenType);
		// 调用 serviceCache 获取单独的App独立的对话记忆，参数1：cacheKey -> 有缓存key就直接调用，若没有就通过参数2创建新的缓存Key
		return serviceCache.get(cacheKey, key -> createAiCodeGeneratorService(appId, codeGenType));
	}


	/**
	 * 使用对话记忆，不同 appId 的对话记忆是独立隔离的，利用LangChain4j有两种实现方案：
	 * 方案2：
	 * 之前所有应用共用同一个Al Service实例，如果想隔离会话记忆，可以给每个应用(app)分配一个专属的Al Service，每个Al Service绑定独立的对话记忆。
	 * <p>
	 * 根据 appId 获取服务
	 *
	 * @param appId       应用Id
	 * @param codeGenType 生成代码类型
	 * @return
	 */
	private AiCodeGeneratorService createAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
		log.info("为 AppId：{} 创建新的 AI 服务实例", appId);

		// 1.根据 appId 构建独立的对话记忆
		MessageWindowChatMemory chatMemory = MessageWindowChatMemory
				.builder()
				.id(appId)
				.chatMemoryStore(redisChatMemoryStore)
				.maxMessages(20)
				.build();

		// 2.从数据库中加载对话历史到会话记忆中
		chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);

		// 3.switch选择生成何种代码类型
		return switch (codeGenType) {
			// 3.1 如果是vue工程，返回单独的对话记忆
			case VUE_PROJECT -> AiServices.builder(AiCodeGeneratorService.class)
					.chatModel(chatModel)
					.streamingChatModel(reasoningStreamingChatModel)
					.chatMemory(chatMemory)
					// 根据不同的对话，设置不同对话记忆 - 框架规定：只要在AI对话上加入了MemoryId，就必须加上MemoryProvider()
					.chatMemoryProvider(memoryId -> chatMemory)
					// tools：把FIleWriteTool方法new出来 -> 将创建的vue工程写到指定路径下
					.tools(
							/*// 写入文件的工具
							new FileWriteTool(),
							// 读取文件的工具
							new FileReadTool(),
							// 修改文件内容的工具
							new FileModifyTool(),
							// 读取文件目录的工具
							new FileDirReadTool(),
							// 删除文件的工具
							new FileDeleteTool()*/

							// 使用 ToolManager 管理全部文件操作工具
							toolManager.getAllTools()
					)
					// 处理工具调用幻觉问题：当AI出现幻觉，调用到不存在的工具时，该怎么处理？ 可以构造一个执行结果，告诉AI没有这个工具
					.hallucinatedToolNameStrategy(toolExecutionRequest ->
							ToolExecutionResultMessage.from(toolExecutionRequest,
									"Error: there is no tool called " + toolExecutionRequest.name()))
					.build();

			// 3.2 对另外两种：直接加载 AI对话记忆
			case HTML, MULTI_FILE -> AiServices.builder(AiCodeGeneratorService.class)
						// 绑定各种方式的大模型对象
						.chatModel(chatModel)
						.streamingChatModel(openAiStreamingChatModel)
						// 根据Id构建独立的对话记忆
						.chatMemory(chatMemory)
						.build();

			// 否则，报错
			default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType);
		};
	}


	/**
	 * 使用对话记忆，不同 appId 的对话记忆是独立隔离的，利用LangChain4j有两种实现方案：
	 * 方案1：内置隔离机制
	 * 在工厂类中创建Al Service 时，我们必须通过chatMemoryProvider为每个memoryld 来构造专属的 MessageWindowChatMemory。
	 * 注意，必须为MessageWindowChatMemory 设置id，因为使用的是同一个 Redis 存储实例，否则Redis中的存储key 都是default，无法区分不同的对话。
	 *
	 * 快速创建AI代码生成器服务
	 *
	 * @return
	 */
	@Bean
	public AiCodeGeneratorService aiCodeGeneratorService() {
		/*return AiServices.builder(AiCodeGeneratorService.class)
				// 绑定各种方式的大模型对象
				.chatModel(chatModel)
				.streamingChatModel(streamingChatModel)
				// 根据Id构建独立的对话记忆
				.chatMemoryProvider(memoryId -> MessageWindowChatMemory
						.builder()
						.id(memoryId)
						.chatMemoryStore(redisChatMemoryStore)
						.maxMessages(20)
						.build())
				.build();*/

		// 方案1依旧保持，根据开闭原则，调用方案2的兼容方法，appId直接指定为0；
		return getAiCodeGeneratorService(0);
	}


	/**
	 * 构造缓存键
	 *
	 * @param appId 应用Id
	 * @param codeGenType 代码类型
	 * @return
	 */
	public String buildCacheKey(long appId, CodeGenTypeEnum codeGenType) {
		return appId + "_" + codeGenType.getValue();
	}
}
