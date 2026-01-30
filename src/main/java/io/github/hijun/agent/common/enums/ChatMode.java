package io.github.hijun.agent.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Chat Mode
 *
 * @author haijun
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2025/12/24 16:51
 * @version 3.4.3
 * @since 3.4.3
 */
@Getter
@AllArgsConstructor
public enum ChatMode implements BaseEnum<String> {
    /**
     * 默认聊天模式
     */
    CHAT("chat", "智能问答"),

    /**
     * h t m l.
     */
    HTML("html", "网页模式"),

    /**
     * p p t.
     */
    PPT("ppt", "PPT模式");

    /**
     * value.
     */
    private final String value;
    /**
     * description.
     */
    private final String desc;
}
