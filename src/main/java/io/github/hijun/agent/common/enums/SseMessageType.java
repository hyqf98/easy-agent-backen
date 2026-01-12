package io.github.hijun.agent.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * SSE消息类型枚举
 *
 * @author haijun
 * @version 3.4.3
 * @date 2025-12-24
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @since 3.4.3
 */
@Getter
@AllArgsConstructor
public enum SseMessageType {
    /**
     * 思考信息
     */
    THINKING("thinking", "思考"),

    /**
     * 工具思考
     */
    TOOL_THROUGH("tool_through", "工具思考"),

    /**
     * 工具调用开始
     *
     * @since 1.0.0-SNAPSHOT
     */
    TOOL_CALL_START("tool_call_start", "工具调用开始"),

    /**
     * 工具调用结果
     *
     * @since 1.0.0-SNAPSHOT
     */
    TOOL_CALL_RESULT("tool_call_result", "工具调用结果"),

    /**
     * 内容流式输出
     *
     * @since 1.0.0-SNAPSHOT
     */
    CONTENT_CHUNK("content_chunk", "输出内容"),

    /**
     * 完成
     *
     * @since 1.0.0-SNAPSHOT
     */
    COMPLETED("completed", "完成"),

    /**
     * 错误
     *
     * @since 1.0.0-SNAPSHOT
     */
    ERROR("error", "错误");

    /**
     * code.
     */
    private final String code;
    /**
     * description.
     */
    private final String description;
}
