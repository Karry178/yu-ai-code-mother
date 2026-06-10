package com.yupi.yucodemotherbackend.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.yupi.yucodemotherbackend.config.CosClientConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * COS 对象存储管理器
 */
@Component
@Slf4j
public class CosManager {

	// 引入COS配置类
	@Resource
	private CosClientConfig cosClientConfig;

	// 引入COS客户端
	@Resource
	private COSClient cosClient;


	/**
	 * 上传对象
	 *
	 * @param key 唯一键 -> 文件路径
	 * @param file 文件
	 * @return 上传结果
	 */
	public PutObjectResult putObject(String key, File file) {
		PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
		return cosClient.putObject(putObjectRequest);
	}


	/**
	 * 上传文件
	 *
	 * @param key 文件路径
	 * @param file 文件
	 * @return
	 */
	public String uploadFile(String key, File file) {
		// 调用上传对象方法，先拿到上传的对象
		PutObjectResult result = putObject(key, file);
		if (result != null) {
			// 结果非空 -> 拿到URL -> 对象存储的域名host和文件路径拼接后才是完整可访问的地址
			String url = String.format("%s%s", cosClientConfig.getHost(), key);
			log.info("文件上传到 COS 成功: {} -> {}", file.getName(), url);
			return url;
		} else {
			log.info("文件上传到 COS 失败：{}", file.getName());
			return null;
		}
	}
}
