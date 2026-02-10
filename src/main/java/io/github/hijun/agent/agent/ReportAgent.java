package io.github.hijun.agent.agent;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.hijun.agent.annotations.Agent;
import io.github.hijun.agent.common.constant.AgentConstants;
import io.github.hijun.agent.common.enums.SseMessageType;
import io.github.hijun.agent.common.enums.StreamTagType;
import io.github.hijun.agent.common.enums.ToolStatus;
import io.github.hijun.agent.entity.AgentContext;
import io.github.hijun.agent.entity.sse.FileContentMessage;
import io.github.hijun.agent.entity.sse.TextMessage;
import io.github.hijun.agent.entity.sse.ToolCallMessage;
import io.github.hijun.agent.utils.StreamTagParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 报告生成智能体
 * <p>
 * 专业的报告生成专家，支持HTML、Markdown、纯文本等多种格式。
 * 从数据文件中提取信息并生成高质量的结构化报告。
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/2/9 14:28
 * @since 1.0.0-SNAPSHOT
 */
@Slf4j
@Agent(value = "report_agent", description = "报告生成专家，支持HTML、Markdown等多种格式的报告生成")
public class ReportAgent extends ReActLLM<FileContentMessage> {

    /**
     * stream tag parser.
     */
    private final StreamTagParser streamTagParser;

    /**
     * ReAct LLM
     *
     * @param chatClient   chat client
     * @param agentContext agent context
     * @since 1.0.0-SNAPSHOT
     */
    public ReportAgent(ChatClient chatClient,
                       AgentContext agentContext) {
        super(chatClient,
                AgentConstants.ReportAgent.SYSTEM_PROMPT,
                AgentConstants.ReAct.NEXT_STEP_PROMPT,
                agentContext);
        this.streamTagParser = new StreamTagParser();
        this.streamTagParser.register(StreamTagType.TOOL_THROUGH);
        this.streamTagParser.register(StreamTagType.REPORT_RESULT);
    }

    /**
     * Think
     * <p>
     * 重写思考方法，添加流式标签解析支持
     *
     * @param messages messages
     * @return list
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    protected List<AssistantMessage.ToolCall> think(List<Message> messages) {
        List<AssistantMessage.ToolCall> list = new ArrayList<>();
        Flux<ChatResponse> responseFlux = this.callLLM(messages, this.agentContext.getToolCallbacks(), false);
        StreamTagParser.Session session = this.streamTagParser.createSession();
        String messageId = UUID.randomUUID().toString();
        AtomicReference<SseMessageType> sseMessageType = new AtomicReference<>(SseMessageType.TOOL_THROUGH);
        responseFlux.doOnNext(chatResponse -> {
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            if (assistantMessage.hasToolCalls()) {
                list.addAll(assistantMessage.getToolCalls());
            }
            String text = assistantMessage.getText();
            if (text != null && !text.isEmpty()) {
                // 使用流式解析器解析文本
                List<StreamTagParser.ParseResult> results = session.parse(text);
                for (StreamTagParser.ParseResult result : results) {
                    String contentToSend = result.content();
                    if (contentToSend == null || contentToSend.isEmpty()) {
                        continue;
                    }
                    // 直接使用枚举类型，无需字符串判断
                    StreamTagType tagType = result.tagType();
                    if (tagType != null) {
                        sseMessageType.set(tagType.getSseMessageType());
                    }
                    // 发送消息
                    TextMessage textMessage = TextMessage.builder()
                            .text(contentToSend)
                            .modelName(this.agentContext.getLlmModel().getModelName())
                            .build();
                    this.agentContext.sendMessage(messageId, sseMessageType.get(), textMessage);
                }
            }
        }).blockLast();
        // 发送完成标记
        TextMessage textMessage = TextMessage.builder()
                .text("<DONE>")
                .modelName(this.agentContext.getLlmModel().getModelName())
                .build();
        this.agentContext.sendMessage(messageId, sseMessageType.get(), textMessage);
        return list;
    }

    /**
     * Action
     * <p>
     * 重写行动方法，添加工具调用状态通知
     *
     * @param toolCalls tool calls
     * @return list
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    protected List<ToolResponseMessage.ToolResponse> action(List<AssistantMessage.ToolCall> toolCalls) {
        String messageId = UUID.randomUUID().toString();
        List<FunctionResponseFuture> toolCallTasks = toolCalls.stream().map(toolCall -> {
            String id = toolCall.id();
            String arguments = toolCall.arguments();
            String type = toolCall.type();
            String name = toolCall.name();
            CompletableFuture<ToolResponseMessage.ToolResponse> future = CompletableFuture.supplyAsync(() -> {
                ToolCallback toolCallback = this.toolCallbackResolver.resolve(name);
                if (toolCallback == null) {
                    throw new NullPointerException("未找到名称为:[" + name + "]，的函数工具");
                }
                ToolCallMessage toolCallMessage = ToolCallMessage
                        .builder()
                        .id(id)
                        .type(type)
                        .name(name)
                        .status(ToolStatus.CALLING)
                        .build();
                this.agentContext.sendMessage(messageId, SseMessageType.TOOL_CALL, toolCallMessage);
                return new ToolResponseMessage.ToolResponse(id, name, toolCallback.call(arguments));
            }).exceptionally(e -> {
                ToolCallMessage toolCallMessage = ToolCallMessage
                        .builder()
                        .id(id)
                        .type(type)
                        .name(name)
                        .status(ToolStatus.FAILED)
                        .errorMessage(e.getMessage())
                        .build();
                this.agentContext.sendMessage(messageId, SseMessageType.TOOL_CALL, toolCallMessage);
                return new ToolResponseMessage.ToolResponse(id, name, e.getMessage());
            });
            return new FunctionResponseFuture(id, name, type, arguments, future);
        }).toList();
        CompletableFuture[] array = toolCallTasks.stream()
                .map(FunctionResponseFuture::getResultFuture)
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(array).join();
        return toolCallTasks.stream().map(task -> {
            String id = task.getId();
            String name = task.getName();
            String inputParams = task.getInputParams();
            try {
                ToolResponseMessage.ToolResponse toolResponse = task.getResultFuture().get();
                ToolCallMessage toolCallMessage = ToolCallMessage
                        .builder()
                        .id(id)
                        .name(name)
                        .params(inputParams)
                        .result(toolResponse.responseData())
                        .status(ToolStatus.SUCCESS)
                        .build();
                this.agentContext.sendMessage(messageId, SseMessageType.TOOL_CALL, toolCallMessage);
                return toolResponse;
            } catch (Exception e) {
                log.error("工具结果获取异常:", e);
                return new ToolResponseMessage.ToolResponse(id, name, e.getMessage());
            }
        }).toList();
    }

    /**
     * Summary
     * <p>
     * 对报告生成结果进行总结，返回文件内容消息
     *
     * @param messages messages
     * @return file content message
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    protected FileContentMessage summary(List<Message> messages) {
        return this.callLLM(messages, Collections.emptyList(), false, new TypeReference<>() {
        });
    }
}
