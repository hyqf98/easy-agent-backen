package io.github.hijun.agent.service.strategy;

import cn.hutool.core.util.StrUtil;
import io.github.hijun.agent.common.Agent;
import io.github.hijun.agent.common.constant.AgentConstants;
import io.github.hijun.agent.common.constant.FileConstants;
import io.github.hijun.agent.common.constant.MessageConstants;
import io.github.hijun.agent.common.enums.ChatMode;
import io.github.hijun.agent.common.enums.SseMessageType;
import io.github.hijun.agent.entity.dto.ContentMessage;
import io.github.hijun.agent.entity.dto.FileCreatedMessage;
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
 * 内容生成智能体。
 *
 * <p>负责根据规划和数据生成最终内容，是报告生成流程的第三步（最后一步）。</p>
 *
 * <h3>职责</h3>
 * <ul>
 *   <li>读取 PlanningAgent 生成的 plan.md 文件</li>
 *   <li>读取 DataCollectAgent 生成的 data.md 文件</li>
 *   <li>根据生成模式（PPT/Markdown/HTML）生成相应格式的内容</li>
 *   <li>将内容保存到会话目录的 content.md 文件</li>
 *   <li>流式发送生成的内容给前端</li>
 * </ul>
 *
 * <h3>输入</h3>
 * <ul>
 *   <li>plan.md 文件（由 PlanningAgent 生成）</li>
 *   <li>data.md 文件（由 DataCollectAgent 生成）</li>
 *   <li>会话 ID（sessionId）</li>
 *   <li>生成模式（chatMode）：PPT/Markdown/HTML</li>
 * </ul>
 *
 * <h3>输出</h3>
 * <ul>
 *   <li>SSE 消息：{@link MessageConstants.SseType#THINKING}（思考过程）</li>
 *   <li>SSE 消息：{@link MessageConstants.SseType#CONTENT_CHUNK}（流式内容）</li>
 *   <li>SSE 消息：FILE_CREATED（content.md）</li>
 *   <li>CallResponse（执行结果，包含 content.md 文件路径）</li>
 * </ul>
 *
 * <h3>最终结果</h3>
 * <p>content.md 文件直接作为用户下载的最终文件。</p>
 *
 * @author hijun
 * @since 1.0.0
 */
@Slf4j
@Component
@Agent(id = AgentConstants.AgentIds.CONTENT_GEN, name = AgentConstants.AgentNames.CONTENT_GEN,
       description = "内容生成智能体，负责生成 PPT/Markdown/HTML 内容")
public class ContentGenAgent extends SimpleAgent {

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
    public ContentGenAgent(ChatClient chatClient, FileTools fileTools) {
        super(chatClient);
        this.fileTools = fileTools;
    }

    /**
     * 执行内容生成任务.
     *
     * @param context 智能体上下文
     * @return 调用响应
     */
    @Override
    public CallResponse execute(AgentContext context) {
        log.info("内容生成智能体开始执行，模式: {}", context.getChatMode());

        SseEmitter emitter = context.getSseEmitter();
        String sessionId = context.getSessionId();
        ChatMode mode = context.getChatMode();

        try {
            // 读取规划和数据文件
            String plan = fileTools.readFile(sessionId + "/plan.md");
            String data = fileTools.readFile(sessionId + "/data.md");

            // 发送思考消息
            sendThinking(emitter, "正在生成" + mode.getDescription() + "内容...");

            // 构建用户提示词
            String userPrompt = buildUserPrompt(plan, data, mode);

            // 调用 LLM 生成内容
            String content = callLLMString(userPrompt, context.getToolCallbacks());

            // 保存内容
            String contentPath = fileTools.writeFileInSession(sessionId, FileConstants.FileType.CONTENT, content);
            log.info("内容已保存到: {}", contentPath);

            // 流式发送内容
            streamContent(emitter, content);

            // 发送文件创建消息
            sendFileCreated(emitter, "content" + FileConstants.FileExtension.MARKDOWN, contentPath, FileConstants.FileType.CONTENT);

            return CallResponse.builder()
                    .success(true)
                    .message("内容生成完成")
                    .data(contentPath)
                    .build();

        } catch (Exception e) {
            log.error("内容生成失败", e);
            sendError(emitter, "内容生成失败: " + e.getMessage());
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
            你是一个专业的内容生成专家。

            你的职责是：
            1. 根据规划文件和采集的数据生成高质量内容
            2. 根据目标格式（PPT/Markdown/HTML）调整内容结构
            3. 确保内容准确、完整、易读

            PPT 模式输出格式：
            ## Slide 1: 标题
            内容...

            ## Slide 2: 标题
            内容...

            Markdown 模式输出格式：
            标准的 Markdown 格式

            HTML 模式输出格式：
            完整的 HTML 代码，包含 CSS 样式
            """;
    }

    /**
     * 构建用户提示词.
     *
     * @param plan 规划内容
     * @param data 数据内容
     * @param mode 生成模式
     * @return 用户提示词
     */
    private String buildUserPrompt(String plan, String data, ChatMode mode) {
        String modeInstruction = switch (mode) {
            case PPT -> "请生成 PPT 格式的内容，每页幻灯片用 ## Slide N: 标题 分隔";
            case MARKDOWN -> "请生成标准的 Markdown 格式内容";
            case HTML -> "请生成完整的 HTML 页面，包含内联 CSS 样式";
            default -> "请生成内容";
        };

        return String.format("""
            规划内容：%s

            采集数据：%s

            %s

            请根据上述信息生成高质量内容。
            """, plan, data, modeInstruction);
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
     * 流式发送内容.
     */
    private void streamContent(SseEmitter emitter, String content) {
        try {
            // 分块流式发送内容
            int chunkSize = 200;
            for (int i = 0; i < content.length(); i += chunkSize) {
                int end = Math.min(i + chunkSize, content.length());
                String chunk = content.substring(i, end);

                ContentMessage message = ContentMessage.builder()
                        .type(SseMessageType.CONTENT_CHUNK)
                        .content(chunk)
                        .build();
                emitter.send(message);
            }
        } catch (IOException e) {
            log.error("流式发送内容失败", e);
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
     * 调用 LLM 返回字符串结果.
     */
    private String callLLMString(String userPrompt, List<org.springframework.ai.tool.ToolCallback> toolCallbacks) {
        UserMessage userMessage = new UserMessage(userPrompt);
        List<Message> messages = List.of(userMessage);
        return callLLM(messages, toolCallbacks, false, String.class);
    }
}
