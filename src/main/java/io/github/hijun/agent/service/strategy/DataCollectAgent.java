package io.github.hijun.agent.service.strategy;

import io.github.hijun.agent.common.Agent;
import io.github.hijun.agent.common.constant.AgentConstants;
import io.github.hijun.agent.common.constant.FileConstants;
import io.github.hijun.agent.common.constant.MessageConstants;
import io.github.hijun.agent.common.enums.SseMessageType;
import io.github.hijun.agent.entity.dto.ContentMessage;
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
import java.util.List;

/**
 * 数据采集智能体。
 *
 * <p>负责根据规划文件调用工具采集相关数据，是报告生成流程的第二步。</p>
 *
 * <h3>职责</h3>
 * <ul>
 * <li>读取 PlanningAgent 生成的 plan.md 文件</li>
 * <li>根据规划中的数据需求调用 MCP 工具采集数据</li>
 * <li>整理和格式化采集到的数据</li>
 * <li>将数据保存到会话目录的 data.md 文件</li>
 * </ul>
 *
 * <h3>输入</h3>
 * <ul>
 * <li>plan.md 文件（由 PlanningAgent 生成）</li>
 * <li>会话 ID（sessionId）</li>
 * <li>MCP 工具回调列表</li>
 * </ul>
 *
 * <h3>输出</h3>
 * <ul>
 * <li>SSE 消息：{@link MessageConstants.SseType#THINKING}（思考过程）</li>
 * <li>SSE 消息：{@link MessageConstants.SseType#TOOL_CALL_START}（工具调用开始）</li>
 * <li>SSE 消息：{@link MessageConstants.SseType#TOOL_CALL_RESULT}（工具调用结果）</li>
 * <li>SSE 消息：FILE_CREATED（data.md）</li>
 * <li>CallResponse（执行结果，包含 data.md 文件路径）</li>
 * </ul>
 *
 * <h3>后续依赖</h3>
 * <p>ContentGenAgent 读取 plan.md 和 data.md 文件作为输入。</p>
 *
 * @author hijun
 * @since 1.0.0
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/1/29 17:52
 * @version 1.0.0-SNAPSHOT
 */
@Slf4j
@Component
@Agent(id = AgentConstants.AgentIds.DATA_COLLECT, name = AgentConstants.AgentNames.DATA_COLLECT,
       description = "数据采集智能体，负责调用工具采集数据")
public class DataCollectAgent extends SimpleAgent {

    /**
     * 文件工具.
     */
    private final FileTools fileTools;

    /**
     * 构造函数.
     *
     * @param chatClient 聊天客户端
     * @param fileTools  文件工具
     * @since 1.0.0-SNAPSHOT
     */
    public DataCollectAgent(ChatClient chatClient, FileTools fileTools) {
        super(chatClient);
        this.fileTools = fileTools;
    }

    /**
     * 执行数据采集任务.
     *
     * @param context 智能体上下文
     * @return 调用响应
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public CallResponse execute(AgentContext context) {
        log.info("数据采集智能体开始执行");

        SseEmitter emitter = context.getSseEmitter();
        String sessionId = context.getSessionId();

        try {
            // 读取规划文件
            String plan = this.fileTools.readFile(sessionId + "/plan.md");

            // 发送思考消息
            this.sendThinking(emitter, "正在根据规划采集数据...");

            // 构建用户提示词
            String userPrompt = this.buildUserPrompt(plan);

            // 调用 LLM 进行数据采集（启用工具调用）
            String data = this.callLLMString(userPrompt, context.getToolCallbacks());

            // 保存数据
            String dataPath = this.fileTools.writeFileInSession(sessionId, FileConstants.FileType.DATA, data);
            log.info("数据已保存到: {}", dataPath);

            return CallResponse.builder()
                    .success(true)
                    .message("数据采集完成")
                    .data(dataPath)
                    .build();

        } catch (Exception e) {
            log.error("数据采集失败", e);
            this.sendError(emitter, "数据采集失败: " + e.getMessage());
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
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    protected String getSystemPrompt() {
        return """
            你是一个专业的数据采集专家。

            你的职责是：
            1. 根据规划文件中的数据需求，使用可用的工具采集数据
            2. 整理和格式化采集到的数据
            3. 确保数据的准确性和完整性

            输出格式要求：
            ## 采集的数据
            （整理后的数据内容）

            ## 数据来源
            - 来源1：...
            - 来源2：...
            """;
    }

    /**
     * 构建用户提示词.
     *
     * @param plan 规划内容
     * @return 用户提示词
     * @since 1.0.0-SNAPSHOT
     */
    private String buildUserPrompt(String plan) {
        return String.format("""
            规划内容：%s

            请根据规划文件中的数据需求，使用可用工具采集相关数据。
            """, plan);
    }

    /**
     * 发送思考消息.
     *
     * @param emitter emitter
     * @param content content
     * @since 1.0.0-SNAPSHOT
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
     * 发送错误消息.
     *
     * @param emitter emitter
     * @param error error
     * @since 1.0.0-SNAPSHOT
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
     * 调用 LLM 返回字符串结果.
     *
     * @param userPrompt user prompt
     * @param toolCallbacks tool callbacks
     * @return string
     * @since 1.0.0-SNAPSHOT
     */
    private String callLLMString(String userPrompt, List<org.springframework.ai.tool.ToolCallback> toolCallbacks) {
        UserMessage userMessage = new UserMessage(userPrompt);
        List<Message> messages = List.of(userMessage);
        // 启用工具调用以进行数据采集
        return this.callLLM(messages, toolCallbacks, true, String.class);
    }
}
