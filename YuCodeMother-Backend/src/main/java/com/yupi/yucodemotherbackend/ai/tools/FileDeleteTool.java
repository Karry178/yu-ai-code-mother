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
 * 文件删除工具 -> 支持AI通过调用的方式删除文件 -> 同时继承自工具基类 BaseTool ，实现3个抽象方法
 */
@Slf4j
@Component // 给每一个实例加Bean
public class FileDeleteTool extends BaseTool{

	@Tool("删除指定路径的文件")
	public String deleteFile(
			@P("文件的相对路径")
			String relativeFilePath,
			@ToolMemoryId Long appId
	) {
		try {
			Path path = Paths.get(relativeFilePath);
			// 1.拿到现在要修改项目的目录
			if (!path.isAbsolute()) {
				String projectDirName = "vue_project_" + appId;
				Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);
				path = projectRoot.resolve(relativeFilePath);
			}
			if (!Files.exists(path)) {
				return "警告：文件不存在，无需删除 - " + relativeFilePath;
			}
			if (!Files.isRegularFile(path)) {
				return "错误：指定路径不是文件，无法删除 - " + relativeFilePath;
			}
			// 2.安全检查：避免删除重要文件
			String fileName = path.getFileName().toString();
			if (isImportantFile(fileName)) {
				return "错误：不允许删除重要文件 - " + fileName;
			}
			Files.delete(path);
			log.info("成功删除文件：{}", path.toAbsolutePath());
			return "文件删除成功：" + relativeFilePath;
		} catch (IOException e) {
			String errorMessage = "删除文件失败：" + relativeFilePath + ", 错误：" + e.getMessage();
			log.error(errorMessage, e);
			return errorMessage;
		}
	}


	/**
	 * 判断是否为重要文件，不允许删除
	 * @param fileName
	 * @return
	 */
	private boolean isImportantFile(String fileName) {
		String[] importantFiles = {
				"package.json", "package-lock.json", "yarn.lock", "pnpm-lock.yaml",
				"vite.config.js", "vite.config.ts", "vue.config.js",
				"tsconfig.json", "tsconfig.app.json", "tsconfig.node.json",
				"index.html", "main.js", "main.ts", "App.vue",".gitignore","README.md"
		};

		for (String important : importantFiles) {
			//
			if (important.equalsIgnoreCase(fileName)) {
				return true;
			}
		}
		return false;
	}


	// 继承自 BaseTool 的方法

	/**
	 * 获取工具的英文名词（对应方法名）
	 *
	 * @return 工具英文名称
	 */
	@Override
	public String getToolName() {
		return "deleteFile";
	}


	/**
	 * 获取工具的中文显示名称
	 *
	 * @return 工具中文名称
	 */
	@Override
	public String getDisplayName() {
		return "删除文件";
	}


	/**
	 * 生成工具执行结果格式 - JSON（保存到数据库）
	 *
	 * @param arguments 工具执行参数
	 * @return 格式化的工具执行结果
	 */
	@Override
	public String generateToolExecutedResult(JSONObject arguments) {
		// 拿到相对路径
		String relativeFilePath = arguments.getStr("relativeFilePath");
		return String.format("【工具调用】%s %s", getDisplayName(), relativeFilePath);
	}
}
