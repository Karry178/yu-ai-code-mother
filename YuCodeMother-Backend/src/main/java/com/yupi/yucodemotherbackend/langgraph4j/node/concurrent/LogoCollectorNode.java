package com.yupi.yucodemotherbackend.langgraph4j.node.concurrent;

import com.yupi.yucodemotherbackend.langgraph4j.model.ImageCollectionPlan;
import com.yupi.yucodemotherbackend.langgraph4j.model.ImageResource;
import com.yupi.yucodemotherbackend.langgraph4j.state.WorkflowContext;
import com.yupi.yucodemotherbackend.langgraph4j.tools.LogoGeneratorTool;
import com.yupi.yucodemotherbackend.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.ArrayList;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 【并发方案2 -> LangGraph4j 并发实现：将每个图片收集工具定义为一个工作节点，这些节点可以并发执行】图片收集节点（并发）
 *
 * -> 【4个图片收集节点】Logo图片收集节点
 */
@Slf4j
public class LogoCollectorNode {

	public static AsyncNodeAction<MessagesState<String>> create() {
		return node_async(state -> {
			WorkflowContext context = WorkflowContext.getContext(state);
			List<ImageResource> logos = new ArrayList<>();
			try {
				ImageCollectionPlan plan = context.getImageCollectionPlan();
				if (plan != null || plan.getLogoTasks() != null) {
					LogoGeneratorTool logoTool = SpringContextUtil.getBean(LogoGeneratorTool.class);
					log.info("开始并发生成Logo，任务数：{}", plan.getLogoTasks().size());
					for (ImageCollectionPlan.LogoTask task : plan.getLogoTasks()) {
						List<ImageResource> images = logoTool.generateLogos(task.description());
						if (images != null) {
							logos.addAll(images);
						}
					}
					log.info("Logo生成完成，共生成 {} 张图片", logos.size());
				}
			} catch (Exception e) {
				log.error("Logo生成失败：{}", e.getMessage(), e);
			}

			context.setLogos(logos);
			context.setCurrentStep("Logo生成");
			return WorkflowContext.saveContext(context);
		});
	}
}
