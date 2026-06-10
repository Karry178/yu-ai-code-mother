package com.yupi.yucodemotherbackend.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WebScreenshotUtilsTest {

	@Test
	void saveWebPageScreenshot() {
		String testUrl = "https://www.codefather.cn";
		String webPageScreenshot = WebScreenshotUtils.saveWebPageScreenshot(testUrl);
		Assertions.assertNotNull(webPageScreenshot);
	}
}