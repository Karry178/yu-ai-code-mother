package com.yupi.yucodemotherbackend.ai.tools;

import cn.hutool.json.JSONObject;
import com.yupi.yucodemotherbackend.constatnt.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件读取工具 -> 支持 AI 通过工具调用的方式读取文件内容 -> 同时继承自工具基类 BaseTool ，实现3个抽象方法
 */
@Slf4j
@Component
public class FileReadTool extends BaseTool{

	@Tool("读取指定路径的文件内容")
	public String readFile(
			@P("文件的相对路径")
			String relativeFilePath,
			@ToolMemoryId Long appId
	) {
		try {
			// 获取相对路径
			Path path = Paths.get(relativeFilePath);
			if (!path.isAbsolute()) {
				String projectDirName = "vue_project_" + appId;
				// 根目录
				Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);
				path = projectRoot.resolve(relativeFilePath);
			}
			if (!Files.exists(path) || !Files.isRegularFile(path)) {
				return "错误：文件不存在或不是文件 - " + relativeFilePath;
			}
			return Files.readString(path);

		} catch (IOException e) {
			String errorMessage = "读取文件失败：" + relativeFilePath + ", 错误：" + e.getMessage();
			log.error(errorMessage, e);
			return errorMessage;
		}
	}


	/**
	 * 获取工具的英文名词（对应方法名）
	 *
	 * @return 工具英文名称
	 */
	@Override
	public String getToolName() {
		return "readFile";
	}


	/**
	 * 获取工具的中文显示名称
	 *
	 * @return 工具中文名称
	 */
	@Override
	public String getDisplayName() {
		return "读取文件";
	}


	/**
	 * 生成工具执行结果格式 - JSON（保存到数据库）
	 *
	 * @param arguments 工具执行参数
	 * @return 格式化的工具执行结果
	 */
	@Override
	public String generateToolExecutedResult(JSONObject arguments) {
		String relativeFilePath = arguments.getStr("relativeFilePath");
		return String.format("【工具调用】%s %s", getDisplayName(), relativeFilePath);
	}
}
