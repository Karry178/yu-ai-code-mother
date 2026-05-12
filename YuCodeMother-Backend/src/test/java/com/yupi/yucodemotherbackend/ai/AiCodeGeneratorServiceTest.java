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
		HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(1, "做个程序员Karry的技术博客，代码要求20行以内！");
		Assertions.assertNotNull(result);
	}

	@Test
	void generateMultiFileCode() {
		MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode("做个程序员Karry的java相关留言板，代码要求20行以内！");
		Assertions.assertNotNull(result);
	}


	@Test
	void testChatMemory() {
		HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(1, "做一个程序员Karry178的Github介绍网页，总共代码不超过50行");
		Assertions.assertNotNull(result);
		result = aiCodeGeneratorService.generateHtmlCode(1, "不要生成网站，告诉我你刚才做了什么？");
		Assertions.assertNotNull(result);
		result = aiCodeGeneratorService.generateHtmlCode(2, "做一个程序员Karry178的Github介绍网页，总共代码不超过40行");
		Assertions.assertNotNull(result);
		result = aiCodeGeneratorService.generateHtmlCode(2, "不要生成网站，告诉我你刚才做了什么？");
		Assertions.assertNotNull(result);
	}
}