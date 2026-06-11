package com.yupi.yucodemotherbackend.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.yupi.yucodemotherbackend.exception.BusinessException;
import com.yupi.yucodemotherbackend.exception.ErrorCode;
import com.yupi.yucodemotherbackend.exception.ThrowUtils;
import com.yupi.yucodemotherbackend.service.ProjectDownloadService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

@Service
@Slf4j
public class ProjectDownloadServiceImpl implements ProjectDownloadService {

	/**
	 * 需要过滤的文件和目录名称
	 */
	private static final Set<String> IGNORED_NAMES = Set.of(
			"node_modules",
			".git",
			"dist",
			"build",
			".DS_Store",
			".env",
			"target",
			".mvn",
			".idea",
			".vscode"
	);


	/**
	 * 需要过滤的文件扩展名
	 */
	private static final Set<String> IGNORED_EXTENSIONS = Set.of(
			".log",
			".tmp",
			".cache"
	);


	/**
	 * 下载项目为压缩包
	 *
	 * @param projectPath 文件路径
	 * @param downloadFileName 文件名
	 * @param response 给前端返回的Http响应
	 * @return
	 */
	@Override
	public void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response) {
		// 1.基础校验
		ThrowUtils.throwIf(StrUtil.isBlank(projectPath), ErrorCode.PARAMS_ERROR, "项目路径不能为空");
		ThrowUtils.throwIf(StrUtil.isBlank(downloadFileName), ErrorCode.PARAMS_ERROR, "下载文件名不能为空");
		File projectDir = new File(projectPath);
		ThrowUtils.throwIf(!projectDir.exists(), ErrorCode.PARAMS_ERROR, "项目路径不存在");
		ThrowUtils.throwIf(!projectDir.isDirectory(), ErrorCode.PARAMS_ERROR, "项目路径不是一个目录");
		log.info("开始打包下载项目：{} -> {}.zip", projectPath, downloadFileName);

		// 2.设置 HTTP 响应头
		response.setStatus(HttpServletResponse.SC_OK);  // 设置返回的状态
		response.setContentType("application/ZIP");  // 设置返回的文件类型
		response.setHeader("Content-Disposition",
				String.format("attachment; filename=\"%s.zip\"", downloadFileName));  // 设置返回的文件名 -> attachment为附件

		// 3.定义文件过滤器
		FileFilter filter = file -> isPathAllowed(projectDir.toPath(), file.toPath());  // 过滤 -> 调用校验路径的方法

		// 4.压缩: 参数 -> 把压缩包生成的响应流返回给前端、指定写的字符类型、是否包含被打包目录、文件过滤器、打包的路径名称
		try {
			// 使用 Hutool 的 ZipUtil 直接将过滤后的目录压缩到响应输入流 给前端
			ZipUtil.zip(response.getOutputStream(), StandardCharsets.UTF_8, false, filter, projectDir);
			log.info("打包下载项目成功：{} -> {}.zip", projectPath, downloadFileName);
		} catch (IOException e) {
			log.error("打包下载项目失败", e);
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "打包下载项目失败");
		}
	}


	/**
	 * 校验路径是否允许包含在压缩包中
	 *
	 * @param projectRoot 项目根目录
	 * @param fullPath    项目完整路径
	 * @return 是否允许
	 */
	private boolean isPathAllowed(Path projectRoot, Path fullPath) {
		// 得到相对路径
		Path relativePath = projectRoot.relativize(fullPath);
		// 检查路径中的每一部分是否符合要求
		for (Path part : relativePath) {
			// 将part转为String类型判断
			String partName = part.toString();
			// 检查是否在忽略名称列表中
			if (IGNORED_NAMES.contains(partName)) {
				return false;
			}
			// 检查是否在忽略扩展名结尾
			if (IGNORED_EXTENSIONS.stream().anyMatch(ext -> partName.toLowerCase().endsWith(ext))) {
				return false;
			}
		}
		return true;
	}
}
