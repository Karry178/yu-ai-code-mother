package com.yupi.yucodemotherbackend.core;

import com.yupi.yucodemotherbackend.ai.AiCodeGeneratorService;
import com.yupi.yucodemotherbackend.ai.model.HtmlCodeResult;
import com.yupi.yucodemotherbackend.ai.model.MultiFileCodeResult;
import com.yupi.yucodemotherbackend.exception.BusinessException;
import com.yupi.yucodemotherbackend.exception.ErrorCode;
import com.yupi.yucodemotherbackend.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import javax.swing.text.html.HTML;
import java.io.File;

/**
 * 设计模式 - 门面模式
 *
 * 代码生成门面类 - 组合代码生成和保存功能
 */
@Service
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

}
