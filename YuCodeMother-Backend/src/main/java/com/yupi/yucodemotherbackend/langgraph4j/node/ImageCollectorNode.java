package com.yupi.yucodemotherbackend.langgraph4j.node;

import com.yupi.yucodemotherbackend.langgraph4j.ai.ImageCollectionPlanService;
import com.yupi.yucodemotherbackend.langgraph4j.model.ImageCollectionPlan;
import com.yupi.yucodemotherbackend.langgraph4j.model.ImageResource;
import com.yupi.yucodemotherbackend.langgraph4j.state.WorkflowContext;
import com.yupi.yucodemotherbackend.langgraph4j.tools.ImageSearchTool;
import com.yupi.yucodemotherbackend.langgraph4j.tools.LogoGeneratorTool;
import com.yupi.yucodemotherbackend.langgraph4j.tools.MermaidDiagramTool;
import com.yupi.yucodemotherbackend.langgraph4j.tools.UndrawIllustrationTool;
import com.yupi.yucodemotherbackend.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 【样板代码】定义工作节点 -> 【自定义】图片收集节点
 *
 *  -> 【并发方案1 -> 工作节点内部实现并发：通过 CompletableFuture 并发调用工具进行收集】图片收集节点（并发）
 */
@Slf4j
public class ImageCollectorNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 图片收集");

            // 【自定义】定义用户原始提示词 + 初始图片列表
            String originalPrompt = context.getOriginalPrompt();
            ArrayList<ImageResource> collectedImages = new ArrayList<>();

            try {
                // 第一步：获取图片收集计划
                    // 【重要】获取AI图片收集服务 -> 自定义一个Spring的上下文获取 静态工具类，自动获取ImageCollectionPlanService的Bean
                ImageCollectionPlanService planService =  SpringContextUtil.getBean(ImageCollectionPlanService.class);
                // 使用 AI 服务进行智能图片收集
                ImageCollectionPlan plan = planService.planImageCollection(originalPrompt);
                log.info("获取到图片收集计划，开始并发执行");

                // 第二步：并发执行各种图片收集任务
                List<CompletableFuture<List<ImageResource>>> futures = new ArrayList<>();
                    // 2.1 并发执行内容图片搜索
                if (plan.getContentImageTasks() != null) {
                    ImageSearchTool imageSearchTool = SpringContextUtil.getBean(ImageSearchTool.class);
                    for (ImageCollectionPlan.ImageSearchTask task : plan.getContentImageTasks()) {
                        // 每调用一次进行一次并发 —— 搜索图片关键词
                        futures.add(CompletableFuture.supplyAsync(() ->
                                imageSearchTool.searchContentImages(task.query())));
                    }
                }
                    // 2.2 并发执行插画图片搜索
                if (plan.getIllustrationTasks() != null) {
                    UndrawIllustrationTool illustrationTool = SpringContextUtil.getBean(UndrawIllustrationTool.class);
                    for (ImageCollectionPlan.IllustrationTask task : plan.getIllustrationTasks()) {
                        // 每调用一次进行一次并发 —— 搜索图片关键词
                        futures.add(CompletableFuture.supplyAsync(() ->
                                illustrationTool.searchIllustrations(task.query())));
                    }
                }

                    // 2.3 并发执行架构图生成
                if (plan.getDiagramTasks() != null) {
                    MermaidDiagramTool diagramTool = SpringContextUtil.getBean(MermaidDiagramTool.class);
                    for (ImageCollectionPlan.DiagramTask task : plan.getDiagramTasks()) {
                        // 每调用一次进行一次并发 —— 根据描述生成mermaid代码架构图
                        futures.add(CompletableFuture.supplyAsync(() ->
                                diagramTool.generateMermaidDiagram(task.mermaidCode(), task.description())));
                    }
                }

                    // 2.4 并发执行Logo图片生成
                if (plan.getIllustrationTasks() != null) {
                    LogoGeneratorTool logoTool = SpringContextUtil.getBean(LogoGeneratorTool.class);
                    for (ImageCollectionPlan.LogoTask task : plan.getLogoTasks()) {
                        // 每调用一次进行一次并发 —— 根据描述生成Logo
                        futures.add(CompletableFuture.supplyAsync(() ->
                                logoTool.generateLogos(task.description())));
                    }
                }

                // 3. 等待所有任务完成并收集结果
                CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                        // 先把并发列表转为数组 ——> 创建一个新的异步任务allTasks
                        futures.toArray(new CompletableFuture[0]));
                allTasks.join();  // X.join() 可以自动实现阻塞 —— 所有任务执行完才到下一步

                // 4.收集所有结果
                for (CompletableFuture<List<ImageResource>> future : futures) {
                    List<ImageResource> images = future.get();
                    if (images != null) {
                        collectedImages.addAll(images);
                    }
                }
                log.info("并发图片收集完毕，共收集到 {} 张图片", collectedImages.size());
            } catch (Exception e) {
                log.error("图片收集失败：{}", e.getMessage(), e);
            }
            
            // 更新操作状态
            context.setCurrentStep("图片收集");
            context.setImageList(collectedImages);
            return WorkflowContext.saveContext(context);
        });
    }
}
