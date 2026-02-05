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
public enum SseMessageType implements BaseEnum<String> {
    /**
     * 思考信息
     */
    THINKING("thinking", "思考"),

    /**
     * 工具思考
     */
    TOOL_THROUGH("tool_through", "工具思考"),

    /**
     * final answer.
     */
    FINAL_ANSWER("final_answer", "最终答案"),

    /**
     * report result.
     */
    REPORT_RESULT("report_result", "报告结果"),

    /**
     * 工具调用开始
     *
     * @since 1.0.0-SNAPSHOT
     */
    TOOL_CALL("tool_call", "工具调用开始"),

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
     * value.
     */
    private final String value;

    /**
     * description.
     */
    private final String desc;
}
