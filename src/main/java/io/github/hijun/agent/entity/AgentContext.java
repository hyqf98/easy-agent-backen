package io.github.hijun.agent.entity;

import io.github.hijun.agent.common.enums.AgentStatus;
import io.github.hijun.agent.common.enums.SseMessageType;
import io.github.hijun.agent.entity.sse.SseMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Sinks;

import java.util.concurrent.atomic.AtomicInteger;

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
     * 会话 ID
     */
    private final String sessionId;

    /**
     * request id.
     */
    private final String requestId;

    /**
     * 最大循环步数
     */
    private static final int MAX_STEPS = 30;

    /**
     * 当前步数
     */
    private final AtomicInteger currentStep = new AtomicInteger(0);

    /**
     * Agent 状态
     */
    private AgentStatus status = AgentStatus.IDLE;

    /**
     * 用户原始消息
     */
    private String userMessage;

    /**
     * Agent Context
     *
     * @param sink      SSE 消息 Sink
     * @param sessionId 会话 ID
     * @since 1.0.0-SNAPSHOT
     */
    public AgentContext(Sinks.Many<SseMessage<?>> sink, String sessionId, String requestId) {
        this.sink = sink;
        this.sessionId = sessionId;
        this.requestId = requestId;
    }

    /**
     * 发送消息（内部方法）
     *
     * @param type    消息类型
     * @param content 消息内容
     * @since 1.0.0-SNAPSHOT
     */
    public void sendMessage(SseMessageType type,
                            Object content) {
        SseMessage<?> sseMessage = SseMessage.builder()
                .sessionId(this.sessionId)
                .requestId(this.requestId)
                .content(content)
                .type(type)
                .build();
        this.sink.tryEmitNext(sseMessage);
    }

    /**
     * 增加步数
     *
     * @return 当前步数
     * @since 1.0.0-SNAPSHOT
     */
    public int incrementStep() {
        return this.currentStep.incrementAndGet();
    }

    /**
     * 获取当前步数
     *
     * @return 当前步数
     * @since 1.0.0-SNAPSHOT
     */
    public int getCurrentStep() {
        return this.currentStep.get();
    }

    /**
     * 是否达到最大步数
     *
     * @return true 如果达到最大步数
     * @since 1.0.0-SNAPSHOT
     */
    public boolean isMaxStepsReached() {
        return this.currentStep.get() >= MAX_STEPS;
    }

    /**
     * 完成 Agent 执行
     *
     * @since 1.0.0-SNAPSHOT
     */
    public void complete() {
        this.setStatus(AgentStatus.FINISHED);
        this.sink.tryEmitComplete();
    }

    /**
     * 发生错误
     *
     * @param error 错误
     * @since 1.0.0-SNAPSHOT
     */
    public void error(Throwable error) {
        this.setStatus(AgentStatus.ERROR);
        this.sink.tryEmitError(error);
    }

    /**
     * 重置上下文（用于复用）
     *
     * @since 1.0.0-SNAPSHOT
     */
    public void reset() {
        this.currentStep.set(0);
        this.status = AgentStatus.IDLE;
        this.userMessage = null;
    }
}
