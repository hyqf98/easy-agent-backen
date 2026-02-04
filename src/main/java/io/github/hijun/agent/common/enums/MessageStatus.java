package io.github.hijun.agent.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息状态枚举
 * <p>
 * 定义消息的处理状态
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Getter
@AllArgsConstructor
public enum MessageStatus implements BaseEnum<String> {

    /**
     * 等待中
     */
    PENDING("pending", "等待中"),

    /**
     * 流式输出中
     */
    STREAMING("streaming", "流式输出中"),

    /**
     * 已完成
     */
    COMPLETED("completed", "已完成");

    /**
     * 状态代码
     */
    private final String value;

    /**
     * 状态描述
     */
    private final String desc;
}
