package com.yupi.yucodemotherbackend.langgraph4j.node;

import com.yupi.yucodemotherbackend.constatnt.AppConstant;
import com.yupi.yucodemotherbackend.core.AiCodeGeneratorFacade;
import com.yupi.yucodemotherbackend.langgraph4j.state.WorkflowContext;
import com.yupi.yucodemotherbackend.model.enums.CodeGenTypeEnum;
import com.yupi.yucodemotherbackend.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 【样板代码】定义工作节点 -> 【自定义】代码生成节点
 */
@Slf4j
public class CodeGeneratorNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 代码生成");
            
            // 【自定义】实际执行代码生成逻辑
            // 1.使用增强提示词作为发给 AI 的用户信息
            String userMessage = context.getEnhancedPrompt();
            CodeGenTypeEnum generationType = context.getGenerationType(); // 网站的生成模式
            // 2.获取 AI 代码生成外观服务 -> 通过Spring上下文工具类获取到 AI生成门面类
            AiCodeGeneratorFacade codeGeneratorFacade = SpringContextUtil.getBean(AiCodeGeneratorFacade.class);
            log.info("开始生成代码，类型：{}（{}）", generationType.getValue(), generationType.getText());
            // 3.先使用固定的 appId (后续再整合到业务中)
            Long appId = 0L;
            // 4.调用流式代码生成
            Flux<String> codeStream = codeGeneratorFacade.generateAndSaveCodeStream(userMessage, generationType, appId);
            // 5.同步等待流式输出完成
            codeStream.blockLast(Duration.ofMinutes(10)); // blockLast()阻塞方法 - 最多等待10min
            // 6.根据类型设置生成目录
            String generatedCodeDir = String.format("%s/%s_%s", AppConstant.CODE_OUTPUT_ROOT_DIR, generationType.getValue(), appId);
            log.info("AI 代码生成完成，生成目录：{}", generatedCodeDir);

            // 更新状态
            context.setCurrentStep("代码生成");
            context.setGeneratedCodeDir(generatedCodeDir);
            return WorkflowContext.saveContext(context);
        });
    }
}
