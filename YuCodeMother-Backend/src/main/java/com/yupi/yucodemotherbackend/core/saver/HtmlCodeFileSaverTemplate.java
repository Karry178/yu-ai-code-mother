package com.yupi.yucodemotherbackend.core.saver;

import cn.hutool.core.util.StrUtil;
import com.yupi.yucodemotherbackend.ai.model.HtmlCodeResult;
import com.yupi.yucodemotherbackend.exception.BusinessException;
import com.yupi.yucodemotherbackend.exception.ErrorCode;
import com.yupi.yucodemotherbackend.model.enums.CodeGenTypeEnum;


/**
 * HTML 代码文件保存期
 */
public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {

	@Override
	protected CodeGenTypeEnum getCodeType() {
		return CodeGenTypeEnum.HTML;
	}


	@Override
	protected void saveFiles(HtmlCodeResult result, String baseDirPath) {
		// 调用父类CodeFileSaverTemplate的writeToFile()方法
		writeToFile(baseDirPath, "index.html", result.getHtmlCode());
	}


	@Override
	protected void validateInput(HtmlCodeResult result) {
		super.validateInput(result);
		// 校验HTML代码不能为空
		if (StrUtil.isBlank(result.getHtmlCode())) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "HTML代码不能为空");
		}
	}
}
