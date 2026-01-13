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
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
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
     * 会话id
     */
    private String sessionId;

    /**
     * 请求id
     */
    private String requestId;

    /**
     * 聊天模式
     */
    private ChatMode chatMode;

    /**
     * 当前运行状态中智能体的状态
     */
    private AgentStatus agentStatus;

    /**
     * 用户任务
     */
    private String userQuery;

    /**
     * 自定的用户提示词
     */
    private String userPrompt;

    /**
     * 用户提交的文件文件信息
     */
    private List<String> userUploadFiles;

    /**
     * 当前智能体执行的步骤
     */
    @Builder.Default
    private Integer concurrentStep = 0;


    /**
     * chat memory.
     */
    @Builder.Default
    private ChatMemory chatMemory = MessageWindowChatMemory.builder()
            .chatMemoryRepository(new InMemoryChatMemoryRepository())
            .build();

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
        this.chatMemory.add(this.sessionId, memory);
    }

    /**
     * Update Memory
     *
     * @param memory memory
     * @since 3.4.3
     */
    public void updateMemory(Message memory) {
        this.chatMemory.add(this.sessionId, memory);
    }

    /**
     * Get Memory
     *
     * @return list
     * @since 1.0.0-SNAPSHOT
     */
    public List<Message> getMemory() {
        return this.chatMemory.get(this.sessionId);
    }

    /**
     * Last Message Is User
     *
     * @return boolean
     * @since 3.4.3
     */
    public boolean lastMessageIsUser() {
        List<Message> messages = this.chatMemory.get(this.sessionId);
        if (CollUtil.isEmpty(messages)) {
            return false;
        }
        Message lastMessage = messages.get(messages.size() - 1);
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
