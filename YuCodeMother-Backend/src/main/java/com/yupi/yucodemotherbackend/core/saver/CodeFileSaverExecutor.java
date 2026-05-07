package com.yupi.yucodemotherbackend.core.saver;

import java.io.File;
import com.yupi.yucodemotherbackend.ai.model.HtmlCodeResult;
import com.yupi.yucodemotherbackend.ai.model.MultiFileCodeResult;
import com.yupi.yucodemotherbackend.exception.BusinessException;
import com.yupi.yucodemotherbackend.exception.ErrorCode;
import com.yupi.yucodemotherbackend.model.enums.CodeGenTypeEnum;

/**
 * 代码文件保存执行器
 * 根据代码生成类型执行相应的保存逻辑
 *
 */
public class CodeFileSaverExecutor {

	private static final HtmlCodeFileSaverTemplate htmlCodeFileSaver = new HtmlCodeFileSaverTemplate();

	private static final MultiFileCodeFileSaverTemplate multiFileCodeFileSaver = new MultiFileCodeFileSaverTemplate();


	public static File executeSaver(Object codeResult, CodeGenTypeEnum codeGenType) {
		return switch (codeGenType) {
			case HTML -> htmlCodeFileSaver.saveCode((HtmlCodeResult) codeResult);
			case MULTI_FILE  -> multiFileCodeFileSaver.saveCode((MultiFileCodeResult) codeResult);
			default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型" + codeGenType);
		};
	}
}
