package io.github.hijun.agent.agent;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import io.github.hijun.agent.common.enums.AgentStatus;
import io.github.hijun.agent.common.enums.SseMessageType;
import io.github.hijun.agent.entity.AgentContext;
import io.github.hijun.agent.entity.sse.TextMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * ReAct LLM
 * <p>
 * 实现 ReAct (Reasoning + Acting) 模式的 LLM Agent
 * 支持循环调度，最多执行 30 步
 *
 * @param <T> t
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026.01.30 18:14
 * @since 1.0.0-SNAPSHOT
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public abstract class ReActLLM<T> extends BaseLLM {

    /**
     * max retry times.
     */
    private static final Integer MAX_RETRY_TIMES = 30;

    /**
     * argument.
     */
    private final Class<T> argument;

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
        // noinspection unchecked
        this.argument = (Class<T>) ClassUtil.getTypeArgument(this.getClass());
    }

    /**
     * 循环执行 ReAct 流程
     * <p>
     * ReAct 循环: Think -> Act -> Next Message -> Think ...
     *
     * @param task user question
     * @return t
     * @since 1.0.0-SNAPSHOT
     */
    public T run(String task) {
        SystemMessage systemMessage = new SystemMessage(this.systemPrompt);
        UserMessage userMessage = new UserMessage(task);
        this.agentContext.addMessage(userMessage);
        this.agentStatus = AgentStatus.RUNNING;
        List<Message> messages = new LinkedList<>();
        messages.add(systemMessage);
        messages.add(userMessage);
        try {
            // 循环执行直到达到停止条件
            while (this.concurrentStep < this.maxStep && this.agentStatus == AgentStatus.RUNNING) {
                try {
                    this.concurrentStep++;
                    log.debug("ReAct 步骤: {}/{}", this.concurrentStep, this.maxStep);
                    List<ToolCall> toolThought = this.think(messages);
                    if (CollectionUtil.isEmpty(toolThought)) {
                        // 内容进行总结响应
                        this.agentStatus = AgentStatus.FINISHED;
                        break;
                    }
                    List<ToolResponse> toolResponses = this.action(toolThought);
                    if (CollectionUtil.isNotEmpty(toolResponses)) {
                        ToolResponseMessage responseMessage = ToolResponseMessage.builder()
                                .responses(toolResponses)
                                .build();
                        messages.add(responseMessage);
                        this.agentContext.addMessage(responseMessage);
                    }
                    messages = this.nextStepMessages(task, toolResponses, messages);
                } catch (Exception e) {
                    this.agentStatus = AgentStatus.ERROR;
                    // 发送异常
                    this.agentContext.error(e);
                }
            }
        } catch (Exception e) {
            log.error("ReAct 循环发生错误: {}", e.getMessage(), e);
            this.agentContext.error(e);
        }
        return this.summary(messages);
    }

    /**
     * 思考阶段
     * <p>
     * 子类需要实现具体的思考逻辑
     * 返回 false 表示应该停止循环
     *
     * @param messages messages
     * @return true 继续执行，false 停止执行
     * @since 1.0.0-SNAPSHOT
     */
    protected List<ToolCall> think(List<Message> messages) {
        List<ToolCall> list = new ArrayList<>();
        String messageId = UUID.randomUUID().toString();
        Flux<ChatResponse> responseFlux = this.callLLM(messages, this.agentContext.getToolCallbacks(), false);
        responseFlux.doOnNext(chatResponse -> {
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            if (assistantMessage.hasToolCalls()) {
                list.addAll(assistantMessage.getToolCalls());
            }
            String text = assistantMessage.getText();
            TextMessage textMessage = TextMessage.builder()
                    .text(text)
                    .modelName(this.agentContext.getLlmModel().getModelName())
                    .build();
            this.agentContext.sendMessage(messageId, SseMessageType.CONTENT_CHUNK, textMessage);
        }).blockLast();
        return list;
    }

    /**
     * 行动阶段
     * <p>
     * 子类需要实现具体的行动逻辑
     *
     * @param toolCalls tool calls
     * @return list
     * @since 1.0.0-SNAPSHOT
     */
    protected List<ToolResponse> action(List<ToolCall> toolCalls) {
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
                return new ToolResponse(id, name, toolCallback.call(arguments));
            }).exceptionally(e -> new ToolResponse(id, name, e.getMessage()));
            return new FunctionResponseFuture(id, name, type, arguments, future);
        }).toList();
        CompletableFuture[] array = toolCallTasks.stream()
                .map(FunctionResponseFuture::getResultFuture)
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(array).join();
        return toolCallTasks.stream().map(task -> {
            String id = task.getId();
            String name = task.getName();
            try {
                return task.getResultFuture().get();
            } catch (Exception e) {
                log.error("工具结果获取异常:", e);
                return new ToolResponse(id, name, e.getMessage());
            }
        }).toList();
    }

    /**
     * 下一步执行消息构建
     *
     * @param toolResponses tool responses
     * @param messages      messages
     * @param task task
     * @return list
     * @since 1.0.0-SNAPSHOT
     */
    protected List<Message> nextStepMessages(String task, List<ToolResponse> toolResponses, List<Message> messages) {
        if (!this.lastMessageIsUser(messages) && StrUtil.isNotEmpty(this.nextStepPrompt)) {
            Map<String, Object> params = Map.of("task", task);
            this.templateRenderer.apply(this.nextStepPrompt, params);
            UserMessage nextMessage = new UserMessage(this.nextStepPrompt);
            messages.add(nextMessage);
        }
        return messages;
    }

    /**
     * 对结果进行总结
     *
     * @param messages messages
     * @return t
     * @since 1.0.0-SNAPSHOT
     */
    protected T summary(List<Message> messages) {
        return this.callLLM(messages, this.agentContext.getToolCallbacks(), false, this.argument);
    }


    /**
     * Last Message Is User
     *
     * @param messages messages
     * @return boolean
     * @since 1.0.0-SNAPSHOT
     */
    protected boolean lastMessageIsUser(List<Message> messages) {
        if (CollectionUtil.isEmpty(messages)) {
            return false;
        }
        return messages.get(messages.size() - 1).getMessageType() == MessageType.USER;
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
    protected static final class FunctionResponseFuture {
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
}
