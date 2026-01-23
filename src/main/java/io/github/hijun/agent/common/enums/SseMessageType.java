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
    ERROR("error", "错误"),

    /**
     * 规划结果
     * <p>规划智能体输出的任务分解结果</p>
     *
     * @since 1.0.0
     */
    PLAN_RESULT("plan_result", "规划结果"),

    /**
     * 文件创建
     * <p>通知前端文件已创建</p>
     *
     * @since 1.0.0
     */
    FILE_CREATED("file_created", "文件创建"),

    /**
     * 智能体切换
     * <p>通知前端智能体正在切换</p>
     *
     * @since 1.0.0
     */
    AGENT_SWITCH("agent_switch", "智能体切换"),

    /**
     * 审查结果
     * <p>审查智能体的审查结果</p>
     *
     * @since 1.0.0
     */
    REVIEW_RESULT("review_result", "审查结果");

    /**
     * code.
     */
    private final String code;
    /**
     * description.
     */
    private final String description;
}
