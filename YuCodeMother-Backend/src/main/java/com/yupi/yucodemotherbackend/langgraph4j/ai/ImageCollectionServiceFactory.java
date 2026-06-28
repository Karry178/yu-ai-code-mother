package com.yupi.yucodemotherbackend.langgraph4j.ai;

import com.yupi.yucodemotherbackend.langgraph4j.tools.ImageSearchTool;
import com.yupi.yucodemotherbackend.langgraph4j.tools.LogoGeneratorTool;
import com.yupi.yucodemotherbackend.langgraph4j.tools.MermaidDiagramTool;
import com.yupi.yucodemotherbackend.langgraph4j.tools.UndrawIllustrationTool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 图片收集服务工厂
 */
@Slf4j
@Configuration
public class ImageCollectionServiceFactory {

	// 引入大模型 + 全部的Tool工具类
	@Resource(name = "openAiChatModel")
	private ChatModel chatModel;

	@Resource
	private ImageSearchTool imageSearchTool;

	@Resource
	private UndrawIllustrationTool undrawIllustrationTool;

	@Resource
	private MermaidDiagramTool mermaidDiagramTool;

	@Resource
	private LogoGeneratorTool logoGeneratorTool;


	/**
	 * 创建图片收集 AI 服务
	 *
	 * @return
	 */
	@Bean
	public ImageCollectionService createImageCollectionService() {
		return AiServices.builder(ImageCollectionService.class)
				.chatModel(chatModel)
				.tools(
						imageSearchTool,
						undrawIllustrationTool,
						mermaidDiagramTool,
						logoGeneratorTool
				)
				.build();
	}
}
