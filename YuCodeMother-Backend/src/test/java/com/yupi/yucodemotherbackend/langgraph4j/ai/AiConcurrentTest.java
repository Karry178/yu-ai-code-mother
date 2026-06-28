package com.yupi.yucodemotherbackend.langgraph4j.ai;

import com.yupi.yucodemotherbackend.ai.AiCodeGenTypeRoutingService;
import com.yupi.yucodemotherbackend.ai.AiCodeGenTypeRoutingServiceFactory;
import com.yupi.yucodemotherbackend.model.enums.CodeGenTypeEnum;
import dev.langchain4j.agent.tool.P;
import jakarta.annotation.Resource;
import jdk.jshell.spi.ExecutionControl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * AI并发处理测试
 */
@Slf4j
@SpringBootTest
public class AiConcurrentTest {

	@Resource
	private AiCodeGenTypeRoutingServiceFactory routingServiceFactory;

	@Test
	public void testConcurrentRoutingCalls() throws ExecutionControl.InternalException, InterruptedException {
		String[] prompts = {
				"做一个简单的HTML界面",
				"做一个多页面网站项目",
				"做一个Vue的管理系统"
		};
		// 使用虚拟线程并发执行
		Thread[] threads = new Thread[prompts.length];
		for (int i = 0; i < prompts.length; i++) {
			final String prompt = prompts[i];
			final int index = i + 1;
			// 使用java21转正的虚拟线程特性
			threads[i] = Thread.ofVirtual().start(() -> {
				// 在虚拟线程中同时调用服务获取结果
				AiCodeGenTypeRoutingService service = routingServiceFactory.createAiCodeGenTypeRoutingService();
				var result = service.routeCodeGenType(prompt);
				log.info("线程 {}：{} -> {}", index, prompt, result.getValue());
			});
		}

		// 等待所有任务完成
		for (Thread thread : threads) {
			thread.join();
		}
	}
}
