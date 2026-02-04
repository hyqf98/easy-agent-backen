package io.github.hijun.agent.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息角色枚举
 * <p>
 * 定义消息的发送者角色
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Getter
@AllArgsConstructor
public enum MessageRole implements BaseEnum<String> {

    /**
     * 助手
     */
    ASSISTANT("assistant", "助手"),

    /**
     * 用户
     */
    USER("user", "用户"),

    /**
     * 工具
     */
    TOOL("tool", "工具");

    /**
     * 角色代码
     */
    private final String value;

    /**
     * 角色描述
     */
    private final String desc;
}
