package com.yupi.yucodemotherbackend.langgraph4j.node.concurrent;

import com.yupi.yucodemotherbackend.langgraph4j.model.ImageCollectionPlan;
import com.yupi.yucodemotherbackend.langgraph4j.model.ImageResource;
import com.yupi.yucodemotherbackend.langgraph4j.state.WorkflowContext;
import com.yupi.yucodemotherbackend.langgraph4j.tools.ImageSearchTool;
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
 * -> 【4个图片收集节点】内容图片收集节点
 */
@Slf4j
public class ContentImageCollectorNode {

	public static AsyncNodeAction<MessagesState<String>> create() {
		return node_async(state -> {
			WorkflowContext context = WorkflowContext.getContext(state);
			List<ImageResource> contentImages = new ArrayList<>();
			try {
				ImageCollectionPlan plan = context.getImageCollectionPlan();
				if (plan != null && plan.getContentImageTasks() != null) {
					ImageSearchTool imageSearchTool = SpringContextUtil.getBean(ImageSearchTool.class);
					log.info("开始并发收集内容图片，任务数：{}", plan.getContentImageTasks());
					for (ImageCollectionPlan.ImageSearchTask task : plan.getContentImageTasks()) {
						List<ImageResource> images = imageSearchTool.searchContentImages(task.query());
						if (images != null) {
							contentImages.addAll(images);
						}
					}
					log.info("内容图片收集完毕，共收集到 {} 张图片", contentImages.size());
				}
			} catch (Exception e) {
				log.error("内容图片收集失败：{}", e.getMessage(), e);
			}

			// 将收集到的图片存储到上下文的中间字段中
			context.setContentImages(contentImages);
			context.setCurrentStep("内容图片收集");
			return WorkflowContext.saveContext(context);
		});
	}
}
