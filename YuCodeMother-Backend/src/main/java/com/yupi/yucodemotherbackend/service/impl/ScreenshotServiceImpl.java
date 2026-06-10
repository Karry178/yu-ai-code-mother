package com.yupi.yucodemotherbackend.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.yupi.yucodemotherbackend.exception.ErrorCode;
import com.yupi.yucodemotherbackend.exception.ThrowUtils;
import com.yupi.yucodemotherbackend.manager.CosManager;
import com.yupi.yucodemotherbackend.service.ScreenshotService;
import com.yupi.yucodemotherbackend.utils.WebScreenshotUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@Service
@Slf4j
public class ScreenshotServiceImpl implements ScreenshotService {

	// 引入 COS 对象存储管理器
	@Resource
	private CosManager cosManager;


	/**
	 * 生成并上传截图到COS对象存储
	 *
	 * @param webUrl 网址
	 * @return
	 */
	@Override
	public String generateAndUploadScreenshot(String webUrl) {
		// 参数校验
		ThrowUtils.throwIf(StrUtil.isBlank(webUrl), ErrorCode.PARAMS_ERROR, "网址不能为空值");
		log.info("开始生成网页截图，URL：{}", webUrl);
		// 生成本地截图 -> 调用工具类的方法
		String localScreenshotPath = WebScreenshotUtils.saveWebPageScreenshot(webUrl);
		ThrowUtils.throwIf(StrUtil.isBlank(localScreenshotPath), ErrorCode.OPERATION_ERROR, "生成网页截图失败");
		// 上传图片到 COS
		try {
			String cosUrl = uploadScreenshotToCos(localScreenshotPath);
			ThrowUtils.throwIf(StrUtil.isBlank(cosUrl), ErrorCode.OPERATION_ERROR, "上传截图到对象存储失败");
			log.info("截图上传成功，URL：{}", cosUrl);
			return cosUrl;
		} finally {
			// 清理本地文件
			cleanupLocalFile(localScreenshotPath);
		}
	}


	/**
	 * 上传截图到对象存储
	 *
	 * @param localScreenshotPath 本地截图路径
	 * @return 对象存储访问URL，失败返回null
	 */
	private String uploadScreenshotToCos(String localScreenshotPath) {
		if (StrUtil.isBlank(localScreenshotPath)) {
			return null;
		}
		File screenshotFile = new File(localScreenshotPath);
		if (!screenshotFile.exists()) {
			log.error("截图文件不存在：{}", localScreenshotPath);
			return null;
		}
		// 生成 COS 对象键
		String fileName = UUID.randomUUID().toString().substring(0, 8) + "_compressed.jpg";
		String cosKey = generateScreenshotKey(fileName);
		return cosManager.uploadFile(cosKey, screenshotFile);
	}


	/**
	 * 生成截图的对象存储键
	 * -> 格式：/screenshots/2026/06/02/filename.jpg
	 *
	 * @param fileName 文件名
	 * @return
	 */
	private String generateScreenshotKey(String fileName) {
		String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		return String.format("/screenshots/%s%s", datePath, fileName);
	}


	/**
	 * 清理本地文件
	 *
	 * @param localFilePath 本地文件路径
	 */
	private void cleanupLocalFile(String localFilePath) {
		File localFile = new File(localFilePath);
		if (localFile.exists()) {
			FileUtil.del(localFile);
			log.info("清理本地文件成功：{}", localFilePath);
		}
	}
}
