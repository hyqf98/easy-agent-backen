package io.github.hijun.agent.entity;

import io.github.hijun.agent.common.enums.SseMessageType;
import io.github.hijun.agent.entity.dto.LlmModelDTO;
import io.github.hijun.agent.entity.sse.SseMessage;
import io.github.hijun.agent.entity.sse.TextMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.tool.ToolCallback;
import reactor.core.publisher.Sinks;

import java.util.List;

/**
 * Agent Context
 * <p>
 * 封装 Agent 执行上下文，提供消息发送能力
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/1/30 13:23
 * @since 1.0.0-SNAPSHOT
 */
@Slf4j
@Data
public class AgentContext {

    /**
     * SSE 消息 Sink
     */
    private final Sinks.Many<SseMessage<?>> sink;

    /**
     * llm model.
     */
    private final LlmModelDTO llmModel;

    /**
     * 会话 ID
     */
    private final String sessionId;

    /**
     * request id.
     */
    private final String requestId;

    /**
     * 用户原始消息
     */
    private String userMessage;


    /**
     * tool callbacks.
     */
    private List<ToolCallback> toolCallbacks;

    /**
     * memory.
     */
    private ChatMemory chatMemory;

    /**
     * Agent Context
     *
     * @param sink      SSE 消息 Sink
     * @param sessionId 会话 ID
     * @param requestId request id
     * @param llmModelDTO llm model d t o
     * @since 1.0.0-SNAPSHOT
     */
    public AgentContext(Sinks.Many<SseMessage<?>> sink,
                        LlmModelDTO llmModelDTO,
                        String sessionId,
                        String requestId) {
        this.sink = sink;
        this.llmModel = llmModelDTO;
        this.sessionId = sessionId;
        this.requestId = requestId;

        this.chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .build();
    }

    /**
     * 发送消息（内部方法）
     *
     * @param type      消息类型
     * @param content   消息内容
     * @param messageId message id
     * @since 1.0.0-SNAPSHOT
     */
    public void sendMessage(String messageId,
                            SseMessageType type,
                            Object content) {
        SseMessage<?> sseMessage = SseMessage.builder()
                .sessionId(this.sessionId)
                .requestId(this.requestId)
                .messageId(messageId)
                .content(content)
                .type(type)
                .build();
        this.sink.tryEmitNext(sseMessage);
    }

    /**
     * Get Messages
     *
     * @return list
     * @since 1.0.0-SNAPSHOT
     */
    public List<Message> getMessages() {
        return this.chatMemory.get(this.sessionId);
    }

    /**
     * Add Messages
     *
     * @param messages messages
     * @since 1.0.0-SNAPSHOT
     */
    public void addMessages(List<Message> messages) {
        this.chatMemory.add(this.sessionId, messages);
    }

    /**
     * Add Message
     *
     * @param message message
     * @since 1.0.0-SNAPSHOT
     */
    public void addMessage(Message message) {
        this.chatMemory.add(this.sessionId, message);
    }

    /**
     * 完成 Agent 执行
     *
     * @since 1.0.0-SNAPSHOT
     */
    public void complete() {
        TextMessage done = TextMessage.builder().text("DONE").build();
        this.sendMessage("", SseMessageType.COMPLETED, done);
        this.sink.tryEmitComplete();
    }

    /**
     * 发生错误
     *
     * @param error 错误
     * @since 1.0.0-SNAPSHOT
     */
    public void error(Throwable error) {
        TextMessage done = TextMessage.builder().text(error.getMessage()).build();
        this.sendMessage("", SseMessageType.ERROR, done);
        this.sink.tryEmitError(error);
    }
}
