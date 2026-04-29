package com.yupi.yucodemotherbackend.core;

import com.yupi.yucodemotherbackend.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

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

	@Test
	void generateAndSaveCodeStream() {
		Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream("生成一个登录界面，尽可能使用20行以内的代码实现。", CodeGenTypeEnum.HTML);
		// 使用collectList().block()方法 可以 等全部代码收集后
		List<String> result = codeStream.collectList().block();
		// 验证结果
		Assertions.assertNotNull(result);
		// 或者把所有代码块拼接后输出
		String completeContent = String.join("", result);
		Assertions.assertNotNull(completeContent);
	}
}