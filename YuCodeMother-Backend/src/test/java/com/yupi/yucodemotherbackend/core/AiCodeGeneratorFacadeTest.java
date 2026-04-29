package com.yupi.yucodemotherbackend.core;

import com.yupi.yucodemotherbackend.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorFacadeTest {

	@Resource
	private AiCodeGeneratorFacade aiCodeGeneratorFacade;

	@Test
	void generateAndSaveCode() {
		File file = aiCodeGeneratorFacade.generateAndSaveCode("生成一个登录界面，尽可能使用30行以内的代码实现。", CodeGenTypeEnum.HTML);
		Assertions.assertNotNull(file);
	}
}