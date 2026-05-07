package com.yupi.yucodemotherbackend.core;

import java.io.File;

import com.yupi.yucodemotherbackend.core.parser.CodeParserExecutor;
import com.yupi.yucodemotherbackend.core.saver.CodeFileSaverExecutor;
import org.springframework.stereotype.Service;

import com.yupi.yucodemotherbackend.ai.AiCodeGeneratorService;
import com.yupi.yucodemotherbackend.ai.model.HtmlCodeResult;
import com.yupi.yucodemotherbackend.ai.model.MultiFileCodeResult;
import com.yupi.yucodemotherbackend.exception.BusinessException;
import com.yupi.yucodemotherbackend.exception.ErrorCode;
import com.yupi.yucodemotherbackend.model.enums.CodeGenTypeEnum;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * 设计模式 - 门面模式
 *
 * 代码生成门面类 - 组合代码生成和保存功能
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

	@Resource
	AiCodeGeneratorService aiCodeGeneratorService;

	/**
	 * 门面类 - 统一入口：根据类型生成并保存代码
	 *
	 * @param userMessage 用户信息
	 * @param codeGenTypeEnum 代码生成类型
	 * @return
	 */
	public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
		// 1.校验
		if (codeGenTypeEnum == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
		}
		// 2.枚举类型
		return switch (codeGenTypeEnum) {
			case HTML -> {
				Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
				// 直接使用Executor执行器保存并返回代码文件
				yield CodeFileSaverExecutor.executeSaver(result, codeGenTypeEnum.HTML);
			}
			case MULTI_FILE -> {
				Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
				yield CodeFileSaverExecutor.executeSaver(result, codeGenTypeEnum.MULTI_FILE);
			}
			default -> {
				String errorMessage = "不支持的生成类型" + codeGenTypeEnum.getValue();
				throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
			}
		};
	}


	/**
	 * 门面类- 统一入口：流式生成并保存代码
	 * @param userMessage 用户信息
	 * @param codeGenTypeEnum 代码生成类型
	 * @return
	 */
	public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
		// 1. 校验
		if (codeGenTypeEnum == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
		}
		// 枚举类型
		return switch (codeGenTypeEnum) {
			case HTML -> {
				Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
				// 使用yield语法越过内容返回值，将结果直接传到外层返回，这一步是调用了执行器返回并保存代码文件
				yield processCodeStream(codeStream, CodeGenTypeEnum.HTML);
			}
			case MULTI_FILE -> {
				// 先从Service层拿到代码流
				Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
				// 调用通用执行器返回并保存文件
				yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE);
			}
			default -> {
				String errorMessage = "不支持的生成类型" + codeGenTypeEnum.getValue();
				throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
			}
		};
	}


/*	*//**
	 * 生成多文件模式的代码并保存
	 *
	 * @param userMessage 用户提示词
	 * @return 保存的目录
	 *//*
	private File generateAndSaveMultiFileCode(String userMessage) {
		MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
		return CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
	}*/


/*	*//**
	 * 生成多文件模式的代码并保存(流式)
	 *
	 * @param userMessage 用户提示词
	 * @return 保存的目录
	 *//*
	private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
		Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
		return processCodeStream(result, CodeGenTypeEnum.MULTI_FILE);
	}*/


	/**
	 * 生成多文件模式的代码并保存(流式)
	 *
	 * @param codeStream 代码流
	 * @param codeGenType 代码生成类型
	 * @return 流式响应
	 */
	private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType) {

		// 字符串拼接器：用于当气流式返回所有的代码之后，再保存代码
		StringBuilder codeBuilder = new StringBuilder();

		// 使用 cache() 缓存流数据，允许多次订阅
		return codeStream.cache()
				.doOnNext(chunk -> {
					// 调用codeBuilder拼接新的代码块 - 实时收集代码片段
					codeBuilder.append(chunk);
				})
				.doOnComplete(() -> {
					// 流式返回后，保存代码
					try {
						String completeCode = codeBuilder.toString();
						// 使用执行器Executor解析代码
						Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
						// 使用执行器保存代码
						File saveDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType);
						// 文件创建完成，打日志
						log.info("文件保存成功，目录为：{}", saveDir.getAbsolutePath());
					} catch (Exception e) {
						log.error("文件保存失败：{}", e.getMessage());
					}
				});
	}
}
