package com.yupi.yucodemotherbackend.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.yupi.yucodemotherbackend.exception.ErrorCode;
import com.yupi.yucodemotherbackend.exception.ThrowUtils;
import com.yupi.yucodemotherbackend.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 抽象代码文件保存器 - 模版方法模式
 *
 * @param <T>
 */
public abstract class CodeFileSaverTemplate<T> {

	// 文件保存的根目录
	private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";


	/**
	 * 保存代码的流程 —— 流程不允许被子类复写，所以使用final修饰
	 *
	 * @param appId 应用Id
	 * @param result 结果
	 * @return
	 */
	public final File saveCode(T result, Long appId) {

		// 1.验证输入
		validateInput(result);
		// 2.构建唯一目录
		String baseDirPath = buildUniqueDir(appId);
		// 3.保存文件 （具体实现方法交给子类，父类不定义如何保存文件）
		saveFiles(result, baseDirPath);
		// 4.返回文件目录对象
		return new File(baseDirPath);
	}


	/**
	 * 保存单个文件
	 *
	 * @param dirPath 目录路径
	 * @param filename 文件名
	 * @param content 文件内容
	 */
	public final void writeToFile(String dirPath, String filename, String content) {
		if (StrUtil.isNotBlank(content)) {
			String filePath = dirPath + File.separator + filename;
			FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
		}
	}


	/**
	 * 校验输入参数（可由子类覆盖），使用protected修饰
	 *
	 * @param result
	 */
	protected void validateInput(T result) {
		ThrowUtils.throwIf(result == null, ErrorCode.SYSTEM_ERROR, "代码结果对象不能为空");
	}


	/**
	 * 构建文件的唯一路径， tmp/code_output/bizType_雪花Id
	 *
	 * @param appId 应用id
	 * @return 目录路径
	 */
	protected String buildUniqueDir(Long appId) {
		ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR, "应用id不能为空值");
		String codeType = getCodeType().getValue();
		String uniqueDirName = StrUtil.format("{}_{}", codeType, appId);
		String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
		FileUtil.mkdir(dirPath);
		return dirPath;
	}


	/**
	 * 抽象方法且仅子类可以实现该方法 - 使用protected
	 *
	 * @param result
	 * @param baseDirPath
	 */
	protected abstract void saveFiles(T result, String baseDirPath);


	/**
	 * 获取代码生成类型（具体实现交给子类）
	 *
	 * @return 代码生成类型枚举
	 */
	protected abstract CodeGenTypeEnum getCodeType();
}
