package com.yupi.yucodemotherbackend.langgraph4j.tools;

import com.yupi.yucodemotherbackend.langgraph4j.model.enums.ImageCategoryEnum;
import com.yupi.yucodemotherbackend.langgraph4j.model.ImageResource;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ImageSearchToolTest {

	// 引入图片搜索工具
	@Resource
	private ImageSearchTool imageSearchTool;

	@Test
	void searchContentImages() {
		// 测试正常搜索
		List<ImageResource> images = imageSearchTool.searchContentImages("phone");
		assertNotNull(images);
		assertFalse(images.isEmpty());
		// 验证返回的图片资源
		ImageResource firstImage = images.get(0);
		assertEquals(ImageCategoryEnum.CONTENT, firstImage.getCategory()); // 测试第一张图片类型
		assertNotNull(firstImage.getDescription()); // 图片描述
		assertNotNull(firstImage.getUrl());
		assertTrue(firstImage.getUrl().startsWith("http")); // 图片网址头
		System.out.println("搜索到" + images.size() + " 张图片");

		images.forEach(image ->
				System.out.println("图片：" + image.getDescription() + " - " + image.getUrl())
		);
	}
}