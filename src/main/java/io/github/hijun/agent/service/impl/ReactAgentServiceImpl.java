package io.github.hijun.agent.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hijun.agent.common.constant.AgentConstants;
import io.github.hijun.agent.common.enums.SseMessageType;
import io.github.hijun.agent.entity.dto.ContentChunkContent;
import io.github.hijun.agent.entity.dto.SseMessage;
import io.github.hijun.agent.entity.dto.ThinkingContent;
import io.github.hijun.agent.entity.dto.ToolCallResultContent;
import io.github.hijun.agent.entity.dto.ToolCallStartContent;
import io.github.hijun.agent.entity.req.ChatRequest;
import io.github.hijun.agent.service.ReactAgentService;
import io.github.hijun.agent.tools.AgentTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * React Agent Service Impl - 基于Spring AI的ReAct实现
 *
 * @author haijun
 * @date 2025-12-24
 * @email "mailto:haijun@email.com"
 * @version 3.4.3
 * @since 3.4.3
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReactAgentServiceImpl implements ReactAgentService {

    /**
     * Thought模式匹配（ReAct格式）
     */
    private static final Pattern THOUGHT_PATTERN = Pattern.compile("Thought:\\s*(.+?)(?=\n(Action|Final Answer)|$)", Pattern.DOTALL);

    /**
     * Final Answer模式匹配
     */
    private static final Pattern FINAL_ANSWER_PATTERN = Pattern.compile("Final Answer:\\s*(.+?)$", Pattern.DOTALL);

    /**
     * chat model.
     */
    private final OpenAiChatModel chatModel;

    /**
     * agent tools.
     */
    private final AgentTools agentTools;

    /**
     * Object mapper for JSON serialization
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * model name.
     */
    @Value("${spring.ai.openai.options.model}")
    private String modelName;

    /**
     * Execute React流程
     *
     * @param request request
     * @return SseEmitter
     * @since 3.4.3
     */
    @Override
    public SseEmitter executeReact(ChatRequest request) {
        SseEmitter emitter = new SseEmitter(AgentConstants.SSE_TIMEOUT);
        String messageId = UUID.randomUUID().toString(true);

        // 异步执行ReAct流程
        CompletableFuture.runAsync(() -> {
            try {
                chat(request.getMessage(), messageId, emitter);
                emitter.complete();
            } catch (Exception e) {
                log.error("ReAct流程异常", e);
                sendSseMessage(emitter, SseMessage.builder()
                        .type(SseMessageType.ERROR)
                        .messageId(messageId)
                        .content(Map.of("error", e.getMessage()))
                        .timestamp(System.currentTimeMillis())
                        .finished(true)
                        .build());
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * ReAct聊天流程
     *
     * @param userInput 用户输入
     * @param messageId 消息ID
     * @param emitter   SSE发射器
     * @since 3.4.3
     */
    private void chat(String userInput, String messageId, SseEmitter emitter) {
        // 1. 初始化工具回调
        ToolCallback[] toolCallbacks = MethodToolCallbackProvider
                .builder()
                .toolObjects(this.agentTools)
                .build()
                .getToolCallbacks();

        Map<String, ToolCallback> toolCallbacksMap = Arrays.stream(toolCallbacks)
                .collect(Collectors.toMap(
                        toolCallback -> toolCallback.getToolDefinition().name(),
                        callback -> callback));

        // 2. 构建ChatClient
        ChatClient chatClient = ChatClient.builder(this.chatModel)
                .defaultOptions(ToolCallingChatOptions.builder()
                        .model(this.modelName)
                        .internalToolExecutionEnabled(false) // 手动管理工具执行
                        .build())
                .defaultToolCallbacks(toolCallbacks)
                .build();

        // 3. 初始化对话历史
        List<Message> conversationHistory = new ArrayList<>();
        conversationHistory.add(new SystemMessage(AgentConstants.SYSTEM_PROMPT_CHAT));
        conversationHistory.add(new UserMessage(userInput));

        // 4. ReAct循环
        int iteration = 0;
        int maxIterations = AgentConstants.MAX_TOOL_CALLS;

        while (iteration < maxIterations) {
            iteration++;
            log.info("ReAct迭代: {}/{}", iteration, maxIterations);

            // 构建Prompt并流式获取响应
            Prompt prompt = new Prompt(conversationHistory);
            ChatResponse chatResponse = chatClient.prompt(prompt).call().chatResponse();

            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            String fullResponse = assistantMessage.getText();

            // 解析和发送思考内容（Thought）
            extractAndSendThinking(fullResponse, messageId, emitter);

            // 检查是否有工具调用
            if (assistantMessage.hasToolCalls()) {
                List<AssistantMessage.ToolCall> toolCalls = assistantMessage.getToolCalls();
                log.info("检测到{}个工具调用", toolCalls.size());

                List<ToolResponseMessage.ToolResponse> toolResponses = new ArrayList<>();

                for (AssistantMessage.ToolCall toolCall : toolCalls) {
                    String toolCallId = toolCall.id();
                    String toolName = toolCall.name();
                    String arguments = toolCall.arguments();

                    log.info("执行工具: {} (ID: {}), 参数: {}", toolName, toolCallId, arguments);

                    // 发送工具调用开始事件
                    sendSseMessage(emitter, SseMessage.builder()
                            .type(SseMessageType.TOOL_CALL_START)
                            .messageId(messageId)
                            .content(ToolCallStartContent.builder()
                                    .toolCallId(toolCallId)
                                    .toolName(toolName)
                                    .arguments(parseArguments(arguments))
                                    .build())
                            .timestamp(System.currentTimeMillis())
                            .build());

                    // 手动执行工具
                    ToolResponseMessage.ToolResponse response = tryToolCall(toolCall, toolCallbacksMap);
                    toolResponses.add(response);

                    // 发送工具调用结果事件
                    sendSseMessage(emitter, SseMessage.builder()
                            .type(SseMessageType.TOOL_CALL_RESULT)
                            .messageId(messageId)
                            .content(ToolCallResultContent.builder()
                                    .toolCallId(toolCallId)
                                    .toolName(toolName)
                                    .result(response.responseData())
                                    .build())
                            .timestamp(System.currentTimeMillis())
                            .build());
                }

                // 将工具响应添加到对话历史
                ToolResponseMessage toolResponseMessage = ToolResponseMessage.builder()
                        .responses(toolResponses)
                        .build();
                conversationHistory.add(assistantMessage);
                conversationHistory.add(toolResponseMessage);
            } else {
                // 没有工具调用，检查是否为最终答案
                String finalAnswer = extractFinalAnswer(fullResponse);

                if (StrUtil.isNotBlank(finalAnswer)) {
                    log.info("检测到Final Answer，ReAct循环完成");

                    // 发送最终答案
                    sendSseMessage(emitter, SseMessage.builder()
                            .type(SseMessageType.CONTENT_CHUNK)
                            .messageId(messageId)
                            .content(ContentChunkContent.builder()
                                    .chunk(finalAnswer)
                                    .build())
                            .timestamp(System.currentTimeMillis())
                            .build());

                    // 发送完成事件
                    sendSseMessage(emitter, SseMessage.builder()
                            .type(SseMessageType.COMPLETED)
                            .messageId(messageId)
                            .content(Map.of("status", "completed"))
                            .timestamp(System.currentTimeMillis())
                            .finished(true)
                            .build());

                    break;
                }

                // 没有Final Answer也没有工具调用，将响应添加到历史继续循环
                conversationHistory.add(assistantMessage);
            }

            // 检查是否达到最大迭代次数
            if (iteration >= maxIterations) {
                log.warn("达到最大迭代次数");
                sendSseMessage(emitter, SseMessage.builder()
                        .type(SseMessageType.COMPLETED)
                        .messageId(messageId)
                        .content(Map.of(
                                "status", "max_iterations_reached",
                                "message", "已达到最大迭代次数，可能需要简化问题"
                        ))
                        .timestamp(System.currentTimeMillis())
                        .finished(true)
                        .build());
                break;
            }
        }
    }

    /**
     * 尝试执行工具调用
     *
     * @param toolCall         工具调用
     * @param toolCallbacksMap 工具回调映射
     * @return 工具响应
     * @since 3.4.3
     */
    private ToolResponseMessage.ToolResponse tryToolCall(
            AssistantMessage.ToolCall toolCall,
            Map<String, ToolCallback> toolCallbacksMap) {
        String id = toolCall.id();
        String name = toolCall.name();
        ToolCallback toolCallback = toolCallbacksMap.get(name);
        if (toolCallback != null) {
            try {
                String result = toolCallback.call(toolCall.arguments());
                return new ToolResponseMessage.ToolResponse(id, name, result);
            } catch (Exception e) {
                log.error("工具执行失败: {}", name, e);
                return new ToolResponseMessage.ToolResponse(id, name, "工具执行失败: " + e.getMessage());
            }
        }
        return new ToolResponseMessage.ToolResponse(id, name, "未找到工具: " + name);
    }

    /**
     * 提取并发送思考内容（Thought）
     *
     * @param response  AI响应
     * @param messageId 消息ID
     * @param emitter   SSE发射器
     * @since 3.4.3
     */
    private void extractAndSendThinking(String response, String messageId, SseEmitter emitter) {
        if (StrUtil.isBlank(response)) {
            return;
        }

        Matcher matcher = THOUGHT_PATTERN.matcher(response);
        if (matcher.find()) {
            String thinking = matcher.group(1).trim();
            if (StrUtil.isNotBlank(thinking)) {
                log.info("检测到思考内容: {}", thinking);
                sendSseMessage(emitter, SseMessage.builder()
                        .type(SseMessageType.THINKING)
                        .messageId(messageId)
                        .content(ThinkingContent.builder()
                                .thought(thinking)  // 使用正确的字段名
                                .build())
                        .timestamp(System.currentTimeMillis())
                        .build());
            }
        }
    }

    /**
     * 提取最终答案（Final Answer）
     *
     * @param response AI响应
     * @return 最终答案内容
     * @since 3.4.3
     */
    private String extractFinalAnswer(String response) {
        if (StrUtil.isBlank(response)) {
            return null;
        }

        Matcher matcher = FINAL_ANSWER_PATTERN.matcher(response);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // 如果响应中包含"Final Answer"但未能正则匹配，返回整个响应
        if (response.contains("Final Answer")) {
            int index = response.indexOf("Final Answer");
            return response.substring(index + "Final Answer".length()).trim().replaceFirst("^:\\s*", "");
        }

        return null;
    }

    /**
     * 发送SSE消息
     *
     * @param emitter SSE发射器
     * @param message 消息对象
     * @since 3.4.3
     */
    private void sendSseMessage(SseEmitter emitter, SseMessage message) {
        try {
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data(message));
        } catch (Exception e) {
            log.error("发送SSE消息失败", e);
        }
    }

    /**
     * 解析JSON字符串为Map
     *
     * @param arguments JSON字符串
     * @return Map对象
     * @since 3.4.3
     */
    private Map<String, Object> parseArguments(String arguments) {
        if (StrUtil.isBlank(arguments)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(arguments, Map.class);
        } catch (Exception e) {
            log.error("解析参数失败: {}", arguments, e);
            return Map.of("raw", arguments);
        }
    }
}
