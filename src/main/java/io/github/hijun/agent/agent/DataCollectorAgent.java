package io.github.hijun.agent.agent;

import io.github.hijun.agent.common.constant.AgentConstants;
import io.github.hijun.agent.common.enums.AgentStatus;
import io.github.hijun.agent.common.enums.SseMessageType;
import io.github.hijun.agent.common.enums.ToolStatus;
import io.github.hijun.agent.entity.AgentContext;
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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Data Collector Agent
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/2/2 18:24
 * @since 1.0.0-SNAPSHOT
 */
@Slf4j
public class DataCollectorAgent extends ReActLLM {

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
    public DataCollectorAgent(ChatClient chatClient,
                              AgentContext agentContext) {
        super(chatClient,
                AgentConstants.ReAct.SYSTEM_PROMPT,
                AgentConstants.ReAct.NEXT_STEP_PROMPT,
                agentContext);
        this.streamTagParser = new StreamTagParser();
        this.streamTagParser.register("<think>", "</think>", AgentConstants.AgentContentType.THINK);
        this.streamTagParser.register("<Final Answer>", "</Final Answer>", AgentConstants.AgentContentType.FINAL_ANSWER);
    }

    /**
     * Think
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
                // 2. 使用流式解析器解析文本
                List<StreamTagParser.ParseResult> results = session.parse(text);
                for (StreamTagParser.ParseResult result : results) {
                    String contentToSend = result.content();
                    if (contentToSend == null || contentToSend.isEmpty()) {
                        continue;
                    }
                    switch (result.type()) {
                        case AgentConstants.AgentContentType.FINAL_ANSWER:
                            sseMessageType.set(SseMessageType.FINAL_ANSWER);
                            this.agentStatus = AgentStatus.FINISHED;
                            break;
                        case AgentConstants.AgentContentType.THINK:
                            sseMessageType.set(SseMessageType.THINKING);
                            break;
                        default:
                            sseMessageType.set(SseMessageType.TOOL_THROUGH);
                            break;
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

        // 发送消息
        TextMessage textMessage = TextMessage.builder()
                .text("DONE")
                .modelName(this.agentContext.getLlmModel().getModelName())
                .build();
        this.agentContext.sendMessage(messageId, sseMessageType.get(), textMessage);

        return list;
    }

    /**
     * Action
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
}
