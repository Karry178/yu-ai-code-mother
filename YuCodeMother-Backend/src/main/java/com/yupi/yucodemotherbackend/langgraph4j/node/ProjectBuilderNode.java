package com.yupi.yucodemotherbackend.langgraph4j.node;

import com.yupi.yucodemotherbackend.core.builder.VueProjectBuilder;
import com.yupi.yucodemotherbackend.langgraph4j.state.WorkflowContext;
import com.yupi.yucodemotherbackend.model.enums.CodeGenTypeEnum;
import com.yupi.yucodemotherbackend.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.io.File;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 【样板代码】定义工作节点 -> 【自定义】项目构建节点
 */
@Slf4j
public class ProjectBuilderNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 项目构建");
            
            // 【自定义】：实际执行项目构建逻辑
            // 1.获取必要的参数
            String generatedCodeDir = context.getGeneratedCodeDir();
            CodeGenTypeEnum generationType = context.getGenerationType();
            String buildResultDir = "";
            // 2.一定是Vue 项目类型：使用 VueProjectBuilder 进行构建 -> 【更新 - 更规范】因为在CodeGenWorkflow类中将[项目构建节点内容]换成了条件边，Vue项目和其余两种做了if-else选择，也就不需要在此类中重复了
            try {
                VueProjectBuilder vueBuilder = SpringContextUtil.getBean(VueProjectBuilder.class);
                // 执行 Vue 项目构建(npm install + npm run build)
                boolean buildSuccess = vueBuilder.buildProject(generatedCodeDir);
                if (buildSuccess) {
                    // 构建成功，返回 dist 目录路径
                    buildResultDir = generatedCodeDir + File.separator + "dist";
                    log.info("Vue 项目构建成功，dist 目录：{}", buildResultDir);
                }
            } catch (Exception e) {
                log.error("Vue 项目构建异常：{}", e.getMessage(), e);
                buildResultDir = generatedCodeDir;
            }

            // 更新状态
            context.setCurrentStep("项目构建"); // 当前步骤状态
            context.setBuildResultDir(buildResultDir);
            log.info("项目构建节点完成：{}", buildResultDir);
            return WorkflowContext.saveContext(context);
        });
    }
}
