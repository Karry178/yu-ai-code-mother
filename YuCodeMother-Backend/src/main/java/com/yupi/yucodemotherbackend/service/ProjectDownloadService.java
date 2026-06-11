package com.yupi.yucodemotherbackend.service;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;

public interface ProjectDownloadService {

	/**
	 * 下载项目为压缩包
	 *
	 * @param projectPath 文件路径
	 * @param fileName 文件名
	 * @param response 给前端返回的Http响应
	 * @return
	 */
	void downloadProjectAsZip(String projectPath, String fileName, HttpServletResponse response);
}
