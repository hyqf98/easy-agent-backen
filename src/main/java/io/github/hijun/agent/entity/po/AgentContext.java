package io.github.hijun.agent.entity.po;

import cn.hutool.core.collection.CollUtil;
import io.github.hijun.agent.common.enums.AgentStatus;
import io.github.hijun.agent.common.enums.ChatMode;
import io.github.hijun.agent.entity.dto.SseMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.LinkedList;
import java.util.List;

/**
 * Agent Context
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:zhonghaijun@zhxx.com"
 * @date 2025/12/30 13:33
 * @since 3.4.3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class AgentContext {

    /**
     * session id.
     */
    private String sessionId;

    /**
     * agent status.
     */
    private AgentStatus agentStatus;

    /**
     * user query.
     */
    private String userQuery;

    /**
     * user prompt.
     */
    private String userPrompt;

    /**
     * chat mode.
     */
    private ChatMode chatMode;

    /**
     * concurrent step.
     */
    @Builder.Default
    private Integer concurrentStep = 0;


    /**
     * memory.
     */
    @Builder.Default
    private List<Message> memory = new LinkedList<>();

    /**
     * 观测待调用工具
     */
    @Builder.Default
    private List<AssistantMessage.ToolCall> observeTools = new LinkedList<>();


    /**
     * 当前对话可用的模型
     */
    private List<ToolCallback> toolCallbacks;

    /**
     * sse emitter.
     */
    private SseEmitter sseEmitter;


    /**
     * Update Memory
     *
     * @param memory memory
     * @since 3.4.3
     */
    public void updateMemory(List<Message> memory) {
        this.memory.addAll(memory);
    }

    /**
     * Update Memory
     *
     * @param memory memory
     * @since 3.4.3
     */
    public void updateMemory(Message memory) {
        this.memory.add(memory);
    }

    /**
     * Last Message Is User
     *
     * @return boolean
     * @since 3.4.3
     */
    public boolean lastMessageIsUser() {
        if (this.memory.isEmpty()) {
            return false;
        }
        Message lastMessage = this.memory.get(this.memory.size() - 1);
        return lastMessage.getMessageType() == MessageType.USER;
    }

    /**
     * Increment Concurrent Step
     *
     * @return integer
     * @since 1.0.0-SNAPSHOT
     */
    public Integer incrementConcurrentStep() {
        return this.concurrentStep++;
    }

    /**
     * Send Message
     *
     * @param sseMessage session
     * @since 3.4.3
     */
    public void sendMessage(SseMessage sseMessage) {
        try {
            this.sseEmitter.send(sseMessage);
        } catch (Exception e) {
            log.error("Send message error: {}", e.getMessage());
        }
    }

    /**
     * Has Tools
     *
     * @return boolean
     * @since 1.0.0-SNAPSHOT
     */
    public boolean hasTools() {
        return CollUtil.isNotEmpty(this.observeTools);
    }

    /**
     * Complete
     *
     * @since 1.0.0-SNAPSHOT
     */
    public void complete() {
        this.sseEmitter.complete();
    }
}
