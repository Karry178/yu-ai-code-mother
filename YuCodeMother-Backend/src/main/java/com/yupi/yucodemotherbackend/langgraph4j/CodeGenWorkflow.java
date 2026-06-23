package com.yupi.yucodemotherbackend.langgraph4j;

import cn.hutool.json.JSONUtil;
import com.yupi.yucodemotherbackend.exception.BusinessException;
import com.yupi.yucodemotherbackend.exception.ErrorCode;
import com.yupi.yucodemotherbackend.langgraph4j.model.QualityResult;
import com.yupi.yucodemotherbackend.langgraph4j.node.*;
import com.yupi.yucodemotherbackend.langgraph4j.state.WorkflowContext;
import com.yupi.yucodemotherbackend.model.enums.CodeGenTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;

/**
 * 【实际可用】代码生成工作流
 */
@Slf4j
public class CodeGenWorkflow {

	/**
	 * 创建完整的工作流
	 */
	public CompiledGraph<MessagesState<String>> createWorkflow() {
		try {
			// 创建工作流图
			return new MessagesStateGraph<String>()
					// 添加节点 - 使用完整实现的工作节点
					.addNode("image_collector", ImageCollectorNode.create())
					.addNode("prompt_enhancer", PromptEnhancerNode.create())
					.addNode("router", RouterNode.create())
					.addNode("code_generator", CodeGeneratorNode.create())
						// 新增点 - 质量检查工作节点
					.addNode("code_quality_check", CodeQualityCheckNode.create())
					.addNode("project_builder", ProjectBuilderNode.create())

					// 添加边
					.addEdge(START, "image_collector")
					.addEdge("image_collector", "prompt_enhancer")
					.addEdge("prompt_enhancer", "router")
					.addEdge("router", "code_generator")
					// 新增边 - 进入到质量检查
					.addEdge("code_generator", "code_quality_check")

						// 使用条件边：根据代码生成类型决定是否需要构建（条件边 -> 代码生成后，写一个判断逻辑）
					.addConditionalEdges("code_quality_check",
							// edge_async(this::routeBuildOrSkip),      // 判断构建或跳过构建后路由
							edge_async(this::routeAfterQualityCheck),   // 质量检查后路由
							Map.of(
									"build", "project_builder",  // 质检通过且需要构建
									"skip_build", END,          // 质检通过但跳过构建
									"fail", "code_generator"    // 质检失败，重新生成
							))

					.addEdge("project_builder", END)

					// 编译工作流
					.compile();
		} catch (GraphStateException e) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "工作流创建失败");
		}
	}


	/**
	 * 执行工作流 -> 实例化工作流才可使用（动态工作流）
	 */
	public WorkflowContext executeWorkflow(String originalPrompt) {
		CompiledGraph<MessagesState<String>> workflow = createWorkflow();

		// 初始化 WorkflowContext
		WorkflowContext initialContext = WorkflowContext.builder()
				.originalPrompt(originalPrompt)
				.currentStep("初始化")
				.build();

		// 显示工作流图
		GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
		log.info("工作流图:\n{}", graph.content());
		log.info("开始执行代码生成工作流");

		// 调用工作流的Stream方法 依次执行工作流
		WorkflowContext finalContext = null;
		int stepCounter = 1;
		for (NodeOutput<MessagesState<String>> step : workflow.stream(Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {
			log.info("--- 第 {} 步完成 ---", stepCounter);
			// 显示当前状态
			WorkflowContext currentContext = WorkflowContext.getContext(step.state());
			if (currentContext != null) {
				finalContext = currentContext;
				log.info("当前步骤上下文: {}", currentContext);
			}
			stepCounter++;
		}
		log.info("工作流执行完成！");
		return finalContext;
	}


	/**
	 * 执行工作流（Flux 流式输出版本）
	 *
	 * @param originalPrompt 原始提示词
	 * @return
	 */
	public Flux<String> executeWorkflowWithFlux(String originalPrompt) {
		return Flux.create(sink -> {
			// 【基础】必须使用异步执行，否则同步执行会把下面任务串行执行，造成阻塞，甚至超过最大响应时间！
			Thread.startVirtualThread(() -> {
				try {
					CompiledGraph<MessagesState<String>> workflow = createWorkflow();
					WorkflowContext initialContext = WorkflowContext.builder()
							.originalPrompt(originalPrompt)
							.currentStep("初始化")
							.build();
					// 每个执行结果通过 sink.next()构造响应流，即指定现在输出的内容给前端
					sink.next(formatSseEvent("workflow_start", Map.of(
							"message", "开始执行代码生成工作流",
							"originalPrompt", originalPrompt
					)));
					GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
					log.info("工作流图：\n{}", graph.content());

					int stepCounter = 1;
					for (NodeOutput<MessagesState<String>> step : workflow.stream(
							Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {
						log.info("--- 第 {} 步完成", stepCounter);
						WorkflowContext currentContext = WorkflowContext.getContext(step.state());
						if (currentContext != null) {
							sink.next(formatSseEvent("step_completed", Map.of(
									"stepNumber", stepCounter,
									"currentStep", currentContext.getCurrentStep()
							)));
							log.info("当前步骤上下文：{}", currentContext);
						}
						stepCounter++;
					}
					sink.next(formatSseEvent("workflow_completed", Map.of(
							"message", "代码生成工作流执行完成！"
					)));
				} catch (Exception e) {
					log.info("工作流执行失败：{}", e.getMessage(), e);
					sink.next(formatSseEvent("workflow_error", Map.of(
							"error", e.getMessage(),
							"message", "工作流执行失败"
					)));
					sink.error(e);
				}
			});
		});
	}


	/**
	 * 格式化 SSE 事件的辅助方法
	 *
	 * @param eventType
	 * @param data
	 * @return
	 */
	private String formatSseEvent(String eventType, Object data) {
		try {
			String jsonData = JSONUtil.toJsonStr(data);
			return "event：" + eventType + "\ndata：" + jsonData + "\n\n";
		} catch (Exception e) {
			log.error("格式化 SSE 事件失败：{}", e.getMessage(), e);
			return "event：error\ndata：{\"error\":\"格式化失败\"}\n\n";
		}
	}


	/**
	 * 执行工作流（SSE 流式输出版本）
	 */
	public SseEmitter executeWorkflowWithSse(String originalPrompt) {
		SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
		Thread.startVirtualThread(() -> {
			try {
				CompiledGraph<MessagesState<String>> workflow = createWorkflow();
				WorkflowContext initialContext = WorkflowContext.builder()
						.originalPrompt(originalPrompt)
						.currentStep("初始化")
						.build();
				sendSseEvent(emitter, "workflow_start", Map.of(
						"message", "开始执行代码生成工作流",
						"originalPrompt", originalPrompt
				));
				GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
				log.info("工作流图:\n{}", graph.content());

				int stepCounter = 1;
				for (NodeOutput<MessagesState<String>> step : workflow.stream(
						Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {
					log.info("--- 第 {} 步完成 ---", stepCounter);
					WorkflowContext currentContext = WorkflowContext.getContext(step.state());
					if (currentContext != null) {
						sendSseEvent(emitter, "step_completed", Map.of(
								"stepNumber", stepCounter,
								"currentStep", currentContext.getCurrentStep()
						));
						log.info("当前步骤上下文: {}", currentContext);
					}
					stepCounter++;
				}
				sendSseEvent(emitter, "workflow_completed", Map.of(
						"message", "代码生成工作流执行完成！"
				));
				log.info("代码生成工作流执行完成！");
				emitter.complete();
			} catch (Exception e) {
				log.error("工作流执行失败: {}", e.getMessage(), e);
				sendSseEvent(emitter, "workflow_error", Map.of(
						"error", e.getMessage(),
						"message", "工作流执行失败"
				));
				emitter.completeWithError(e);
			}
		});
		return emitter;
	}

	/**
	 * 发送 SSE 事件的辅助方法
	 */
	private void sendSseEvent(SseEmitter emitter, String eventType, Object data) {
		try {
			emitter.send(SseEmitter.event()
					.name(eventType)
					.data(data));
		} catch (IOException e) {
			log.error("发送 SSE 事件失败: {}", e.getMessage(), e);
		}
	}


	/**
	 * 根据代码生成类型决定是否需要构建（条件边需要的条件）
	 *
	 * @param state 当前状态
	 * @return
	 */
	private String routeBuildOrSkip(MessagesState<String> state) {
		WorkflowContext context = WorkflowContext.getContext(state);
		CodeGenTypeEnum generationType = context.getGenerationType();
		// HTML 和 MULTI_FILE 类型不需要构建，直接结束
		if (generationType == CodeGenTypeEnum.HTML || generationType == CodeGenTypeEnum.MULTI_FILE) {
			return "skip_build";
		}
		// VUE_PROJECT 需要构建
		return "build";
	}


	/**
	 * 根据质检结果决定下一步走向
	 *
	 * @param state
	 * @return
	 */
	private String routeAfterQualityCheck(MessagesState<String> state) {

		// 1.先获取工作流状态，然后获取质检结果
		WorkflowContext context = WorkflowContext.getContext(state);
		QualityResult qualityResult = context.getQualityResult();
		// 2.如果质检失败，重新生成代码
		if (qualityResult == null || !qualityResult.getIsValid()) {
			log.error("代码质检失败，需要重新生成代码");
			return "fail";
		}
		// 3.质检通过，使用原有的构建路由逻辑
		log.info("代码质检通过，继续后续流程 - 根据代码生成类型决定是否需要构建");
		return routeBuildOrSkip(state);
	}
}