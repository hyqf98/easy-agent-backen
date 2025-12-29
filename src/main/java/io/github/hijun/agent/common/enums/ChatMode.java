package io.github.hijun.agent.common.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * Chat Mode
 *
 * @author haijun
 * @email "mailto:haijun@email.com"
 * @date 2025/12/24 16:51
 * @version 3.4.3
 * @since 3.4.3
 */
@Getter
public enum ChatMode {
    /**
     * 默认聊天模式
     */
    CHAT("chat", "聊天模式"),

    /**
     * 报告生成模式
     */
    REPORT("report", "报告生成");

    /**
     * code.
     */
    private final String code;
    /**
     * description.
     */
    private final String description;

    /**
     * Chat Mode
     *
     * @param code code
     * @param description description
     * @since 3.4.3
     */
    ChatMode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 模式代码
     * @return 聊天模式枚举，未找到则返回CHAT
     * @since 3.4.3
     */
    public static ChatMode fromCode(String code) {
        return Arrays.stream(values())
                .filter(mode -> mode.code.equals(code))
                .findFirst()
                .orElse(CHAT);
    }
}
