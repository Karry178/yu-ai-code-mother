package com.yupi.yucodemotherbackend.ai;

import com.yupi.yucodemotherbackend.ai.model.HtmlCodeResult;
import com.yupi.yucodemotherbackend.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiCodeGeneratorServiceTest {

	// 引入AiCodeGeneratorService
	@Resource
	private AiCodeGeneratorService aiCodeGeneratorService;

	@Test
	void generateHtmlCode() {
		HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode("做个程序员Karry的技术博客，代码要求20行以内！");
		Assertions.assertNotNull(result);
	}

	@Test
	void generateMultiFileCode() {
		MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode("做个程序员Karry的java相关留言板，代码要求20行以内！");
		Assertions.assertNotNull(result);
	}
}