package io.github.hijun.agent.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 流式标签类型枚举
 * <p>
 * 用于 StreamTagParser 注册标签解析策略，每个标签类型对应一个 SSE 消息类型。
 * 支持多智能体复用相同的标签定义。
 * </p>
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/2/10
 * @since 1.0.0-SNAPSHOT
 */
@Getter
@AllArgsConstructor
public enum StreamTagType implements BaseEnum<String> {

    /**
     * 思考标签 - 用于包裹推导过程
     */
    THINK("think", "思考标签", "\n\n", "</Think>", SseMessageType.THINKING),

    /**
     * 工具思考标签 - 用于包裹工具调用思考过程
     */
    TOOL_THROUGH("tool_through", "工具思考标签", "<ToolThrough>", "</ToolThrough>", SseMessageType.TOOL_THROUGH),

    /**
     * 最终答案标签 - 用于包裹最终答案
     */
    FINAL_ANSWER("final_answer", "最终答案标签", "<Final Answer>", "</Final Answer>", SseMessageType.FINAL_ANSWER),

    /**
     * 报告结果标签 - 用于包裹报告内容
     */
    REPORT_RESULT("report_result", "报告结果标签", "<Report>", "</Report>", SseMessageType.REPORT_RESULT);

    /**
     * 类型值（用于内部标识）
     */
    private final String value;

    /**
     * 描述
     */
    private final String desc;

    /**
     * 开始标签
     */
    private final String startTag;

    /**
     * 结束标签
     */
    private final String endTag;

    /**
     * 对应的SSE消息类型
     */
    private final SseMessageType sseMessageType;

    /**
     * 根据 SseMessageType 查找对应的 StreamTagType
     *
     * @param sseMessageType SSE 消息类型
     * @return 对应的 StreamTagType，如果找不到则返回 null
     * @since 1.0.0-SNAPSHOT
     */
    public static StreamTagType fromSseMessageType(SseMessageType sseMessageType) {
        if (sseMessageType == null) {
            return null;
        }
        for (StreamTagType tagType : values()) {
            if (tagType.getSseMessageType() == sseMessageType) {
                return tagType;
            }
        }
        return null;
    }
}
