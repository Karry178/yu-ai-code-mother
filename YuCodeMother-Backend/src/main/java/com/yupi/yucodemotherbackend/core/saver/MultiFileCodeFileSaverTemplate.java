package com.yupi.yucodemotherbackend.core.saver;

import cn.hutool.core.util.StrUtil;
import com.yupi.yucodemotherbackend.ai.model.MultiFileCodeResult;
import com.yupi.yucodemotherbackend.exception.BusinessException;
import com.yupi.yucodemotherbackend.exception.ErrorCode;
import com.yupi.yucodemotherbackend.model.enums.CodeGenTypeEnum;

/**
 * 多文件代码保留器
 */
public class MultiFileCodeFileSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult> {

	@Override
	protected CodeGenTypeEnum getCodeType() {
		return CodeGenTypeEnum.MULTI_FILE;
	}


	@Override
	protected void saveFiles(MultiFileCodeResult result, String baseDirPath) {
		// 保存HTML文件
		writeToFile(baseDirPath, "index.html", result.getHtmlCode());
		// 保存CSS文件
		writeToFile(baseDirPath, "index.css", result.getCssCode());
		// 保存JS文件
		writeToFile(baseDirPath, "index.js", result.getJsCode());
	}


	@Override
	protected void validateInput(MultiFileCodeResult result) {
		super.validateInput(result);
		// 至少要有HTML代码，CSS代码和JS代码可以为空
		if (StrUtil.isBlank(result.getHtmlCode())) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码内容不能为空");
		}
	}


}
