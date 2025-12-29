package io.github.hijun.agent.common.enums;

import lombok.Getter;

/**
 * SSE消息类型枚举
 *
 * @author haijun
 * @date 2025-12-24
 * @email "mailto:haijun@email.com"
 * @version 3.4.3
 * @since 3.4.3
 */
@Getter
public enum SseMessageType {
    /**
     * 思考中
     */
    THINKING("thinking", "思考中"),

    /**
     * 工具调用开始
     */
    TOOL_CALL_START("tool_call_start", "工具调用开始"),

    /**
     * 工具调用结果
     */
    TOOL_CALL_RESULT("tool_call_result", "工具调用结果"),

    /**
     * 内容流式输出
     */
    CONTENT_CHUNK("content_chunk", "内容输出"),

    /**
     * 报告内容更新
     */
    REPORT_UPDATE("report_update", "报告更新"),

    /**
     * 完成
     */
    COMPLETED("completed", "完成"),

    /**
     * 错误
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

    /**
     * Sse Message Type
     *
     * @param code code
     * @param description description
     * @since 3.4.3
     */
    SseMessageType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
