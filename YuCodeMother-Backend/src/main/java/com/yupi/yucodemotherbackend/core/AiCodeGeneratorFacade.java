package com.yupi.yucodemotherbackend.core;

import java.io.File;

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
			case HTML -> generateAndSaveHtmlCode(userMessage);
			case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
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
			case HTML -> generateAndSaveHtmlCodeStream(userMessage);
			case MULTI_FILE -> generateAndSaveMultiFileCodeStream(userMessage);
			default -> {
				String errorMessage = "不支持的生成类型" + codeGenTypeEnum.getValue();
				throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
			}
		};
	}


	/**
	 * 生成HTML模式的代码并保存
	 *
	 * @param userMessage 用户提示词
	 * @return 保存的目录
	 */
	private File generateAndSaveHtmlCode(String userMessage) {
		HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
		return CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
	}


	/**
	 * 生成多文件模式的代码并保存
	 *
	 * @param userMessage 用户提示词
	 * @return 保存的目录
	 */
	private File generateAndSaveMultiFileCode(String userMessage) {
		MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
		return CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
	}


	/**
	 * 生成HTML模式的代码并保存(流式)
	 *
	 * @param userMessage 用户提示词
	 * @return 保存的目录
	 */
	private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
		Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
		// 字符串拼接器：用于当气流式返回所有的代码之后，再保存代码
		StringBuilder codeBuilder = new StringBuilder();
		
		// 使用 cache() 缓存流数据，允许多次订阅
		return result.cache()
				.doOnNext(chunk -> {
					// 调用codeBuilder拼接新的代码块 - 实时收集代码片段
					codeBuilder.append(chunk);
				})
				.doOnComplete(() -> {
					try {
						// 流式返回后，保存代码
						String completeHtmlCode = codeBuilder.toString(); // 把收集完的代码进行字符串转换
						// 使用代码解析器的方法 CodeParser() 解析代码
						HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(completeHtmlCode);
						// 调用文件保存器，得到一个完整的文件对象
						File saveDir = CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);

						// 文件创建完成，打日志
						log.info("文件保存成功，目录为：{}", saveDir.getAbsolutePath());
					} catch (Exception e) {
						log.error("文件保存失败：{}", e.getMessage());
					}
				});
	}


	/**
	 * 生成多文件模式的代码并保存(流式)
	 *
	 * @param userMessage 用户提示词
	 * @return 保存的目录
	 */
	private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
		Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
		// 字符串拼接器：用于当气流式返回所有的代码之后，再保存代码
		StringBuilder codeBuilder = new StringBuilder();
		
		// 使用 cache() 缓存流数据，允许多次订阅
		return result.cache()
				.doOnNext(chunk -> {
					// 调用codeBuilder拼接新的代码块 - 实时收集代码片段
					codeBuilder.append(chunk);
				})
				.doOnComplete(() -> {
					try {
						// 流式返回后，保存代码
						String completeMultiFileCode = codeBuilder.toString();
						// 使用代码解析器的方法 CodeParser() 解析代码
						MultiFileCodeResult multiFileCodeResult = CodeParser.parseMultiFileCode(completeMultiFileCode);
						// 调用文件保存器，得到一个完整的文件对象
						File saveDir = CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
						// 文件创建完成，打日志
						log.info("文件保存成功，目录为：{}", saveDir.getAbsolutePath());
					} catch (Exception e) {
						log.error("文件保存失败：{}", e.getMessage());
					}
				});
	}
}
