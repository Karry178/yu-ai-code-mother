package com.yupi.yucodemotherbackend.langgraph4j.node;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.yupi.yucodemotherbackend.langgraph4j.model.ImageResource;
import com.yupi.yucodemotherbackend.langgraph4j.state.WorkflowContext;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.List;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;


/**
 * 【样板代码】定义工作节点 -> 【自定义】提示词增强节点
 */
@Slf4j
public class PromptEnhancerNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 提示词增强");
            
            // 实际执行提示词增强逻辑
            // 1.获取原始提示词和图片列表
            String originalPrompt = context.getOriginalPrompt();
            String imageListStr = context.getImageListStr(); // 图片列表目前按照字符串传递
            List<ImageResource> imageList = context.getImageList(); // 兼容处理：后续图片列表修改为正常的imageList传递（并发收集）
            // 2.构建增强后的提示词
            StringBuilder enhancedPromptBuilder = new StringBuilder();
            enhancedPromptBuilder.append(originalPrompt);
            // 3.如果有图片列表，则拼接每一张图片信息
            if (CollUtil.isNotEmpty(imageList) || StrUtil.isNotBlank(imageListStr)) {
                // 给增强版提示词添加内容
                enhancedPromptBuilder.append("\n\n## 可用素材资源\n");
                enhancedPromptBuilder.append("请在生成网站使用以下图片资源，将这些图片合理的嵌入到网站的相应位置中。\n");
                if (CollUtil.isNotEmpty(imageList)) {
                    // 4.通过图片列表拼接每一张图片的增强提示词信息
                    for (ImageResource image : imageList) {
                        enhancedPromptBuilder.append("- ")
                                .append(image.getCategory().getText())
                                .append(": ")
                                .append(image.getDescription())
                                .append("（")
                                .append(image.getUrl())
                                .append("）\n");
                    }
                } else {
                    // 否则，直接拼接
                    enhancedPromptBuilder.append(imageListStr);
                }
            }

            // 将构造的 增强提示词 转字符串
            String enhancedPrompt = enhancedPromptBuilder.toString();
            // 更新状态
            context.setCurrentStep("提示词增强"); // 当前步骤状态
            context.setEnhancedPrompt(enhancedPrompt);
            log.info("提示词增强完成，增强后长度：{} 字符", enhancedPrompt.length());
            return WorkflowContext.saveContext(context);
        });
    }
}
