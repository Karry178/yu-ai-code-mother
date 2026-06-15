package com.yupi.yucodemotherbackend.langgraph4j.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import com.yupi.yucodemotherbackend.exception.BusinessException;
import com.yupi.yucodemotherbackend.exception.ErrorCode;
import com.yupi.yucodemotherbackend.langgraph4j.enums.ImageCategoryEnum;
import com.yupi.yucodemotherbackend.langgraph4j.model.ImageResource;
import com.yupi.yucodemotherbackend.manager.CosManager;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mermaid 架构图生成工具
 */
@Slf4j
@Component
public class MermaidDiagramTool {

	@Resource
	private CosManager cosManager;

	/**
	 * Mermaid 架构图生成工具 -> 为什么只返回一个架构图却用List列表返回？ -> 因为每一个工具类返回的均为 List ，可以将所有图片返回的放在一起，可以合并为一整个List
	 *
	 * @param mermaidCode Mermaid 图标代码
	 * @param description  架构图描述
	 * @return
	 */
	@Tool("将 Mermaid 代码转换为架构图图片，用于展示系统结构和技术关系")
	public List<ImageResource> generateMermaidDiagram(@P("Mermaid 图标代码") String mermaidCode,
	                                                  @P("架构图描述") String description) {
		if (StrUtil.isBlank(mermaidCode)) {
			return new ArrayList<>();
		}

		try {
			// 调用方法 将 Mermaid代码 转换为 SVG 图片
			File diagramFile = convertMermaidToSvg(mermaidCode);
			// 上传到 COS对象存储
			String keyName = String.format("/mermaid/%s/%s",
					RandomUtil.randomString(5), diagramFile.getName());
			String cosUrl = cosManager.uploadFile(keyName, diagramFile);
			// 清理临时文件
			FileUtil.del(diagramFile);

			// 构造出架构图图片对象
			if (StrUtil.isNotBlank(cosUrl)) {
				return Collections.singletonList(ImageResource.builder()
								.category(ImageCategoryEnum.ARCHITECTURE) // 架构图片类型
								.description(description)
								.url(cosUrl) // Url
								.build());
			}
		} catch (Exception e) {
			log.error("生成架构图失败：{}", e.getMessage(), e);
		}
		return new ArrayList<>();
	}


	/**
	 * 将 Mermaid 代码转换为 SVG 图片
	 *
	 * @param mermaidCode mermaid代码
	 * @return SVG图片
	 */
	private File convertMermaidToSvg(String mermaidCode) {
		// 创建临时输入文件 -> 定义原始的mermaid命令
		File tempInputFile = FileUtil.createTempFile("mermaid_input_", ".mmd", true);
		FileUtil.writeUtf8String(mermaidCode, tempInputFile);
		// 创建临时输出文件
		File tempOutputFile = FileUtil.createTempFile("mermaid_output_", ".svg", true);
		// 根据操作系统选择命令
		String command = SystemUtil.getOsInfo().isWindows() ? "mmdc.cmd" : "mmdc";
		// 构建命令
		String cmdLine = String.format("%s -i %s -o %s -b transparent",
				command,
				tempInputFile.getAbsoluteFile(),
				tempOutputFile.getAbsoluteFile()
		);
		// 执行命令
		RuntimeUtil.execForStr(cmdLine);
		// 检查输出文件
		if (!tempOutputFile.exists() || tempOutputFile.length() == 0) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Mermaid CLI 执行失败");
		}
		// 清理输入文件，保留输出文件供上传使用
		FileUtil.del(tempInputFile);
		return tempOutputFile;
	}
}


