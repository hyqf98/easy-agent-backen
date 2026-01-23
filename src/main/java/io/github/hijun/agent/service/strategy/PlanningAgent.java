package io.github.hijun.agent.service.strategy;

import cn.hutool.core.util.StrUtil;
import io.github.hijun.agent.common.Agent;
import io.github.hijun.agent.common.constant.AgentConstants;
import io.github.hijun.agent.common.constant.FileConstants;
import io.github.hijun.agent.common.constant.MessageConstants;
import io.github.hijun.agent.common.enums.SseMessageType;
import io.github.hijun.agent.entity.dto.ContentMessage;
import io.github.hijun.agent.entity.dto.FileCreatedMessage;
import io.github.hijun.agent.entity.dto.PlanResultMessage;
import io.github.hijun.agent.entity.po.AgentContext;
import io.github.hijun.agent.entity.po.CallResponse;
import io.github.hijun.agent.tools.FileTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 规划智能体。
 *
 * <p>负责分析用户需求并制定内容生成计划，是报告生成流程的第一步。</p>
 *
 * <h3>职责</h3>
 * <ul>
 *   <li>分析用户需求，理解生成目标</li>
 *   <li>制定详细的生成计划（包含步骤、预估时间）</li>
 *   <li>将规划结果保存到会话目录的 plan.md 文件</li>
 * </ul>
 *
 * <h3>输入</h3>
 * <ul>
 *   <li>用户需求（userPrompt）</li>
 *   <li>会话 ID（sessionId）- 用于文件隔离</li>
 * </ul>
 *
 * <h3>输出</h3>
 * <ul>
 *   <li>SSE 消息：{@link MessageConstants.SseType#THINKING}（思考过程）</li>
 *   <li>SSE 消息：FILE_CREATED（plan.md）</li>
 *   <li>SSE 消息：PLAN_RESULT（规划结果）</li>
 *   <li>CallResponse（执行结果，包含 plan.md 文件路径）</li>
 * </ul>
 *
 * <h3>后续依赖</h3>
 * <p>DataCollectAgent 读取 plan.md 文件作为输入。</p>
 *
 * @author hijun
 * @since 1.0.0
 */
@Slf4j
@Component
@Agent(id = AgentConstants.AgentIds.PLANNING, name = AgentConstants.AgentNames.PLANNING,
       description = "规划智能体，负责分析需求并制定生成计划")
public class PlanningAgent extends SimpleAgent {

    /**
     * 文件工具.
     */
    private final FileTools fileTools;

    /**
     * 构造函数.
     *
     * @param chatClient 聊天客户端
     * @param fileTools  文件工具
     */
    public PlanningAgent(ChatClient chatClient, FileTools fileTools) {
        super(chatClient);
        this.fileTools = fileTools;
    }

    /**
     * 执行规划任务.
     *
     * @param context 智能体上下文
     * @return 调用响应
     */
    @Override
    public CallResponse execute(AgentContext context) {
        log.info("规划智能体开始执行，用户请求: {}", context.getUserQuery());

        SseEmitter emitter = context.getSseEmitter();
        String sessionId = context.getSessionId();

        try {
            // 发送思考消息
            sendThinking(emitter, "正在分析您的需求，制定生成计划...");

            // 构建用户提示词
            String userPrompt = buildUserPrompt(context);

            // 调用 LLM 进行规划
            String plan = callLLMString(userPrompt, context.getToolCallbacks());

            // 保存规划结果到文件
            String planPath = fileTools.writeFileInSession(sessionId, FileConstants.FileType.PLAN, plan);
            log.info("规划结果已保存到: {}", planPath);

            // 发送文件创建消息
            sendFileCreated(emitter, "plan" + FileConstants.FileExtension.MARKDOWN, planPath, FileConstants.FileType.PLAN);

            // 发送规划结果消息
            sendPlanResult(emitter, extractPlanSummary(plan), extractSteps(plan));

            // 返回结果
            return CallResponse.builder()
                    .success(true)
                    .message("规划完成")
                    .data(planPath)
                    .build();

        } catch (Exception e) {
            log.error("规划智能体执行失败", e);
            sendError(emitter, "规划失败: " + e.getMessage());
            return CallResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    /**
     * 获取系统提示词.
     *
     * @return 系统提示词
     */
    @Override
    protected String getSystemPrompt() {
        return """
            你是一个专业的任务规划专家。

            你的职责是：
            1. 仔细分析用户的需求，理解用户想要生成什么类型的内容
            2. 根据生成模式（PPT/Markdown/HTML）制定相应的生成策略
            3. 确定需要采集的数据类型和来源
            4. 制定详细的执行步骤

            输出格式要求：
            ## 规划概述
            （简要描述你的理解和计划）

            ## 执行步骤
            1. 第一步...
            2. 第二步...
            ...

            ## 数据需求
            - 需要的数据类型1
            - 需要的数据类型2

            ## 预期产出
            - 最终产出的文件类型
            - 内容结构概述
            """;
    }

    /**
     * 构建用户提示词.
     *
     * @param context 智能体上下文
     * @return 用户提示词
     */
    private String buildUserPrompt(AgentContext context) {
        return String.format("""
            用户需求：%s

            生成模式：%s

            请根据上述需求制定详细的生成计划。
            """, context.getUserQuery(), context.getChatMode().getDescription());
    }

    /**
     * 发送思考消息.
     */
    private void sendThinking(SseEmitter emitter, String content) {
        try {
            ContentMessage message = ContentMessage.builder()
                    .type(SseMessageType.THINKING)
                    .content(content)
                    .build();
            emitter.send(message);
        } catch (IOException e) {
            log.error("发送思考消息失败", e);
        }
    }

    /**
     * 发送文件创建消息.
     */
    private void sendFileCreated(SseEmitter emitter, String fileName, String filePath, String fileType) {
        try {
            FileCreatedMessage message = new FileCreatedMessage();
            message.setType(SseMessageType.FILE_CREATED);
            message.setFileName(fileName);
            message.setFilePath(filePath);
            message.setFileType(fileType);
            emitter.send(message);
        } catch (IOException e) {
            log.error("发送文件创建消息失败", e);
        }
    }

    /**
     * 发送规划结果消息.
     */
    private void sendPlanResult(SseEmitter emitter, String plan, List<String> steps) {
        try {
            PlanResultMessage message = new PlanResultMessage();
            message.setType(SseMessageType.PLAN_RESULT);
            message.setPlan(plan);
            message.setSteps(steps);
            message.setEstimatedTime(steps.size() * 30);
            emitter.send(message);
        } catch (IOException e) {
            log.error("发送规划结果消息失败", e);
        }
    }

    /**
     * 发送错误消息.
     */
    private void sendError(SseEmitter emitter, String error) {
        try {
            ContentMessage message = ContentMessage.builder()
                    .type(SseMessageType.ERROR)
                    .content(error)
                    .build();
            emitter.send(message);
        } catch (IOException e) {
            log.error("发送错误消息失败", e);
        }
    }

    /**
     * 提取规划摘要.
     */
    private String extractPlanSummary(String plan) {
        String[] lines = plan.split("\n");
        for (String line : lines) {
            if (StrUtil.isNotBlank(line) && !line.startsWith("#")) {
                return line.trim();
            }
        }
        return plan.substring(0, Math.min(100, plan.length()));
    }

    /**
     * 提取执行步骤.
     */
    private List<String> extractSteps(String plan) {
        List<String> steps = new ArrayList<>();
        String[] lines = plan.split("\n");
        for (String line : lines) {
            if (line.matches("^\\d+\\..*")) {
                steps.add(line.trim());
            }
        }
        return steps;
    }

    /**
     * 调用 LLM 返回字符串结果.
     */
    private String callLLMString(String userPrompt, List<org.springframework.ai.tool.ToolCallback> toolCallbacks) {
        UserMessage userMessage = new UserMessage(userPrompt);
        List<Message> messages = List.of(userMessage);
        return callLLM(messages, toolCallbacks, false, String.class);
    }
}
