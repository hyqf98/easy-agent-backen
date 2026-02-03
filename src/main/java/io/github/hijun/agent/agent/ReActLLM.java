package io.github.hijun.agent.agent;

import cn.hutool.core.collection.CollectionUtil;
import io.github.hijun.agent.common.constant.AgentConstants;
import io.github.hijun.agent.common.enums.AgentStatus;
import io.github.hijun.agent.common.enums.SseMessageType;
import io.github.hijun.agent.common.enums.ToolStatus;
import io.github.hijun.agent.entity.AgentContext;
import io.github.hijun.agent.entity.sse.TextMessage;
import io.github.hijun.agent.entity.sse.ToolCallMessage;
import io.github.hijun.agent.utils.StreamTagParser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.AssistantMessage.ToolCall;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage.ToolResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.resolution.StaticToolCallbackResolver;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * ReAct LLM
 * <p>
 * 实现 ReAct (Reasoning + Acting) 模式的 LLM Agent
 * 支持循环调度，最多执行 30 步
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026.01.30 18:14
 * @since 1.0.0-SNAPSHOT
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public abstract class ReActLLM extends BaseLLM {

    /**
     * max retry times.
     */
    private static final Integer MAX_RETRY_TIMES = 30;

    /**
     * agent context.
     */
    protected final AgentContext agentContext;

    /**
     * tool callback resolver.
     */
    protected final ToolCallbackResolver toolCallbackResolver;

    /**
     * max step.
     */
    protected Integer maxStep = MAX_RETRY_TIMES;

    /**
     * system prompt.
     */
    private final String systemPrompt;

    /**
     * next step prompt.
     */
    private final String nextStepPrompt;

    /**
     * agent status.
     */
    protected AgentStatus agentStatus;

    /**
     * concurrent step.
     */
    protected Integer concurrentStep = 0;


    /**
     * ReAct LLM
     *
     * @param chatClient     chat client
     * @param agentContext   agent context
     * @param systemPrompt   system prompt
     * @param nextStepPrompt next step prompt
     * @since 1.0.0-SNAPSHOT
     */
    public ReActLLM(ChatClient chatClient,
                    String systemPrompt,
                    String nextStepPrompt,
                    AgentContext agentContext) {
        super(chatClient);
        this.agentContext = agentContext;
        this.systemPrompt = systemPrompt;
        this.nextStepPrompt = nextStepPrompt;
        this.toolCallbackResolver = new StaticToolCallbackResolver(agentContext.getToolCallbacks());
    }

    /**
     * 循环执行 ReAct 流程
     * <p>
     * ReAct 循环: Think -> Act -> Observe -> Think ...
     * 最多执行 30 步，直到达到停止条件
     *
     * @param userQuestion user question
     * @since 1.0.0-SNAPSHOT
     */
    public void run(String userQuestion) {
        SystemMessage systemMessage = new SystemMessage(this.systemPrompt);

        UserMessage userMessage = new UserMessage(userQuestion);
        this.agentContext.addMessage(userMessage);
        this.agentStatus = AgentStatus.RUNNING;
        try {
            List<Message> messages = new LinkedList<>();
            messages.add(systemMessage);
            messages.add(userMessage);
            // 循环执行直到达到停止条件
            while (this.concurrentStep < this.maxStep && this.agentStatus == AgentStatus.RUNNING) {
                try {
                    String messageId = "messageId-" + UUID.randomUUID();
                    // 增加步数
                    this.concurrentStep++;
                    log.debug("ReAct 步骤: {}/{}", this.concurrentStep, this.maxStep);
                    if (!this.lastMessageIsUser(messages)) {
                        UserMessage nextMessage = new UserMessage(this.nextStepPrompt);
                        messages.add(nextMessage);
                    }

                    List<ToolCall> toolThought = this.think(messageId, messages);
                    if (CollectionUtil.isEmpty(toolThought)) {
                        // 内容进行总结响应
                        this.summary(messages);
                        this.agentStatus = AgentStatus.FINISHED;
                        break;
                    }
                    List<ToolResponse> toolResponses = this.action(messageId, toolThought);
                    if (CollectionUtil.isNotEmpty(toolResponses)) {
                        ToolResponseMessage responseMessage = ToolResponseMessage.builder()
                                .responses(toolResponses)
                                .build();
                        messages.add(responseMessage);
                        this.agentContext.addMessage(responseMessage);
                    }
                } catch (Exception e) {
                    this.agentStatus = AgentStatus.ERROR;
                    // 发送异常
                }
            }
            this.agentContext.complete();
        } catch (Exception e) {
            log.error("ReAct 循环发生错误: {}", e.getMessage(), e);
            this.agentContext.error(e);
        }
    }

    /**
     * 对结果进行总结
     *
     * @param messages messages
     * @since 1.0.0-SNAPSHOT
     */
    protected void summary(List<Message> messages) {

    }

    /**
     * Last Message Is User
     *
     * @param messages messages
     * @return boolean
     * @since 1.0.0-SNAPSHOT
     */
    private boolean lastMessageIsUser(List<Message> messages) {
        if (CollectionUtil.isEmpty(messages)) {
            return false;
        }
        return messages.get(messages.size() - 1).getMessageType() == MessageType.USER;
    }

    /**
     * 思考阶段
     * <p>
     * 子类需要实现具体的思考逻辑
     * 返回 false 表示应该停止循环
     *
     * @param messages messages
     * @param messageId message id
     * @return true 继续执行，false 停止执行
     * @since 1.0.0-SNAPSHOT
     */
    private List<ToolCall> think(String messageId, List<Message> messages) {
        List<ToolCall> list = new ArrayList<>();
        Flux<ChatResponse> responseFlux = this.callLLM(messages, this.agentContext.getToolCallbacks(), false);
        StreamTagParser streamTagParser = new StreamTagParser();
        responseFlux.doOnNext(chatResponse -> {
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            if (assistantMessage.hasToolCalls()) {
                list.addAll(assistantMessage.getToolCalls());
            }
            String text = assistantMessage.getText();
            if (text != null && !text.isEmpty()) {
                // 2. 使用流式解析器解析文本
                List<StreamTagParser.ParseResult> results = streamTagParser.parse(text);
                for (StreamTagParser.ParseResult result : results) {
                    String contentToSend = result.content();
                    if (contentToSend == null || contentToSend.isEmpty()) {
                        continue;
                    }
                    SseMessageType sseMessageType;
                    switch (result.type()) {
                        case AgentConstants.AgentContentType.FINAL_ANSWER:
                            sseMessageType = SseMessageType.FINAL_ANSWER;
                            this.agentStatus = AgentStatus.FINISHED;
                            break;
                        case AgentConstants.AgentContentType.THINK:
                            sseMessageType = SseMessageType.THINKING;
                            break;
                        default:
                            sseMessageType = SseMessageType.TOOL_THROUGH;
                            break;
                    }
                    // 发送消息
                    TextMessage textMessage = TextMessage.builder()
                            .text(contentToSend)
                            .modelName("")
                            .build();
                    this.agentContext.sendMessage(messageId, sseMessageType, textMessage);
                }
            }
        }).blockLast();
        return list;
    }

    /**
     * Do Think
     *
     * @param chatResponse chat response
     * @since 1.0.0-SNAPSHOT
     */
    protected void doThink(ChatResponse chatResponse) {

    }

    /**
     * 行动阶段
     * <p>
     * 子类需要实现具体的行动逻辑
     *
     * @param toolCalls tool calls
     * @param messageId message id
     * @return list
     * @since 1.0.0-SNAPSHOT
     */
    private List<ToolResponse> action(String messageId, List<ToolCall> toolCalls) {
        List<FunctionResponseFuture> toolCallTasks = toolCalls.stream().map(toolCall -> {
            String id = toolCall.id();
            String arguments = toolCall.arguments();
            String type = toolCall.type();
            String name = toolCall.name();
            CompletableFuture<ToolResponse> future = CompletableFuture.supplyAsync(() -> {
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
                return new ToolResponse(id, name, toolCallback.call(arguments));
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
                return new ToolResponse(id, name, e.getMessage());
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
                ToolResponse toolResponse = task.getResultFuture().get();
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
                return new ToolResponse(id, name, e.getMessage());
            }
        }).toList();
    }


    /**
     * Action Before
     *
     * @since 1.0.0-SNAPSHOT
     */
    protected void actionBefore() {

    }

    /**
     * Action After
     *
     * @since 1.0.0-SNAPSHOT
     */
    private void actionAfter() {

    }

    /**
     * Action Error
     *
     * @since 1.0.0-SNAPSHOT
     */
    private void actionError() {

    }

    /**
     * Function Response Future
     *
     * @author haijun
     * @version 1.0.0-SNAPSHOT
     * @email "mailto:iamxiaohaijun@gmail.com"
     * @date 2026/2/2 18:05
     * @since 1.0.0-SNAPSHOT
     */
    @Data
    @AllArgsConstructor
    private static final class FunctionResponseFuture {
        /**
         * id.
         */
        private String id;
        /**
         * name.
         */
        private String name;
        /**
         * type.
         */
        private String type;

        /**
         * input params.
         */
        private String inputParams;

        /**
         * result future.
         */
        private CompletableFuture<ToolResponse> resultFuture;
    }


    /**
     * Inner Agent Context
     *
     * @author haijun
     * @date 2026/2/3 20:22
     * @version 1.0.0-SNAPSHOT
     * @since 1.0.0-SNAPSHOT
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    protected static class InnerAgentContext {
        /**
         * message id.
         */
        private String messageId;
    }
}
