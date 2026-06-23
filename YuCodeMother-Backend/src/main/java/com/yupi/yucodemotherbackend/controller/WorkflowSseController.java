package com.yupi.yucodemotherbackend.controller;

import com.yupi.yucodemotherbackend.langgraph4j.CodeGenWorkflow;
import com.yupi.yucodemotherbackend.langgraph4j.state.WorkflowContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.apache.bcel.classfile.Code;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.awt.*;

@RestController
@RequestMapping("/workflow")
@Slf4j
public class WorkflowSseController {

	/**
	 * 同步执行工作流
	 *
	 * @param prompt 用户输入提示词
	 * @return
	 */
	@PostMapping("/execute")
	public WorkflowContext executeWorkflow(@RequestParam String prompt) {
		log.info("收到同步工作流执行请求：{}", prompt);
		return new CodeGenWorkflow().executeWorkflow(prompt);
	}


	/**
	 * Flux 流式执行工作流
	 *
	 * @param prompt 用户输入提示词
	 * @return
	 */
	@GetMapping(value = "/execute-flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> executeWorkflowWithFlux(@RequestParam String prompt) {
		log.info("收到 Flux 工作流执行请求：{}", prompt);
		return new CodeGenWorkflow().executeWorkflowWithFlux(prompt);
	}


	/**
	 * SSE 流式执行工作流
	 *
	 * @param prompt
	 * @return
	 */
	@GetMapping(value = "/execute-sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter executeWorkflowWithSse(@RequestParam String prompt) {
		log.info("收到 SSE 工作流执行请求：{}", prompt);
		return new CodeGenWorkflow().executeWorkflowWithSse(prompt);
	}
}
