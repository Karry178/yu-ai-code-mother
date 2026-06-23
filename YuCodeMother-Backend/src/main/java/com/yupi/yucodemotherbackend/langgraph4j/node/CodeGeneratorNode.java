package com.yupi.yucodemotherbackend.langgraph4j.node;

import com.yupi.yucodemotherbackend.constatnt.AppConstant;
import com.yupi.yucodemotherbackend.core.AiCodeGeneratorFacade;
import com.yupi.yucodemotherbackend.langgraph4j.model.QualityResult;
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


    /**
     * 构造用户提示词
     *
     * @param context
     * @return
     */
    private static String buildUserMessage(WorkflowContext context) {
        String userMessage = context.getEnhancedPrompt();
        // 检查是否存在质检失败结果
        QualityResult qualityResult = context.getQualityResult();
        if (isQualityCheckFailed(qualityResult)) {
            // 直接将错误修复信息作为新的提示词（起到了修改的作用）
            userMessage = buildErrorFixPrompt(qualityResult);
        }
        return userMessage;
    }


    /**
     * 判断质检是否失败
     *
     * @param qualityResult 质量结果
     * @return
     */
    private static boolean isQualityCheckFailed(QualityResult qualityResult) {
        return qualityResult != null &&
                !qualityResult.getIsValid() &&
                qualityResult.getErrors() != null &&
                !qualityResult.getErrors().isEmpty();
    }


    /**
     * 构造错误修复提示词
     *
     * @param qualityResult 质量结果
     * @return
     */
    private static String buildErrorFixPrompt(QualityResult qualityResult) {
        // 构造新提示词
        StringBuilder errorInfo = new StringBuilder();
        errorInfo.append("\n\n##上次生成的代码存在以下问题，请修复：\n");
        // 添加错误列表
        qualityResult.getErrors().forEach(error ->
                errorInfo.append("- ").append(error).append("\n"));
        // 添加修复建议(如果有)
        if (qualityResult.getSuggestions() != null && !qualityResult.getSuggestions().isEmpty()) {
            errorInfo.append("\n## 修复建议：\n");
            qualityResult.getSuggestions().forEach(suggestion ->
                    errorInfo.append("- ").append(suggestion).append("\n"));
        }
        errorInfo.append("\n请根据上述问题和建议重新生成代码，确保修复所有提到的问题。");
        return errorInfo.toString();
    }
}
