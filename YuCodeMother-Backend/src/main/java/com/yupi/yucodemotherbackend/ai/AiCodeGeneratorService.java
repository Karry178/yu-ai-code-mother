package com.yupi.yucodemotherbackend.ai;

import com.yupi.yucodemotherbackend.ai.model.HtmlCodeResult;
import com.yupi.yucodemotherbackend.ai.model.MultiFileCodeResult;

import dev.langchain4j.service.SystemMessage;
import reactor.core.publisher.Flux;

public interface AiCodeGeneratorService {

	/**
	 * 生成 HTML 代码
	 *
	 * @param userMessage 用户提示词
	 * @return AI的输出结果
	 */
	@SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")  // 系统提示词注解，最好是直接指定prompt的路径，而不是在这里面写提示词
	HtmlCodeResult generateHtmlCode(String userMessage);


	/**
	 * 生成多个文件的代码
	 *
	 * @param userMessage 用户提示词
	 * @return AI的输出结果
	 */
	@SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
	MultiFileCodeResult generateMultiFileCode(String userMessage);


	/**
	 * 流式生成 HTML 代码，Flux<> 数据流输出
	 * @param userMessage 用户提示词
	 * @return
	 */
	@SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
	Flux<String> generateHtmlCodeStream(String userMessage);


	/**
	 * 流式生成多个文件的代码
	 * @param userMessage 用户提示词
	 * @return
	 */
	@SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
	Flux<String> generateMultiFileCodeStream(String userMessage);
}
