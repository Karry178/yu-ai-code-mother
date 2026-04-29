package com.yupi.yucodemotherbackend.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.yupi.yucodemotherbackend.ai.model.HtmlCodeResult;
import com.yupi.yucodemotherbackend.ai.model.MultiFileCodeResult;
import com.yupi.yucodemotherbackend.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 文件保存器
 */
public class CodeFileSaver {

	// 文件保存的根目录
	private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

	/**
	 * 保存HTML网页代码
	 *
	 * @param htmlCodeResult HTML 代码生成结果
	 * @return
	 */
	public static File saveHtmlCodeResult(HtmlCodeResult htmlCodeResult) {
		// 分别调用方法buildUniqueDir、writeToFile实现构造文件唯一路径 + 保存单个文件的功能
		String baseDirPath = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
		writeToFile(baseDirPath, "index.html", htmlCodeResult.getHtmlCode());
		return new File(baseDirPath);
	}


	/**
	 * 保存多文件网页代码
	 *
	 * @param multiFileCodeResult 多文件网页代码生成结果
	 * @return
	 */
	public static File saveMultiFileCodeResult(MultiFileCodeResult multiFileCodeResult) {
		String baseDirPath = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
		writeToFile(baseDirPath, "index.html", multiFileCodeResult.getHtmlCode());
		writeToFile(baseDirPath, "style.css", multiFileCodeResult.getCssCode());
		writeToFile(baseDirPath, "script.js", multiFileCodeResult.getJsCode());
		return new File(baseDirPath);
	}


	/**
	 * 构建文件的唯一路径(tmp/code_output/业务类型bizType + 雪花ID)
	 *
	 * @param bizType 业务类型
	 * @return
	 */
	private static String buildUniqueDir(String bizType) {
		// 文件唯一名称 = 拼接特定格式字符串 + 业务类型 + 雪花Id
		String uniqueDirName = StrUtil.format("{}_{}", bizType + IdUtil.getSnowflakeNextIdStr());
		// 文件路径名称 = 根目录 + 文件路径分隔符 + 文件唯一名称
		String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
		// 创建出文件
		FileUtil.mkdir(dirPath);
		return dirPath;
	}


	/**
	 * 保存单个文件的方法
	 *
	 * @param dirPath 文件路径
	 * @param filename 文件名
	 * @param content 文件内容
	 */
	public static void writeToFile(String dirPath, String filename, String content) {
		// 文件路径 = 基础路径 + File的分隔符 + 文件名
		String filePath = dirPath + File.separator + filename;

		// 写入文件内容
		FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
	}
}
