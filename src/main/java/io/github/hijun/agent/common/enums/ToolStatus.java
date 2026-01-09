package io.github.hijun.agent.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Tool Status
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:haijun@email.com"
 * @date 2026/1/8 14:16
 * @since 1.0.0-SNAPSHOT
 */
@Getter
@AllArgsConstructor
public enum ToolStatus {

    /**
     * c a l l i n g.
     */
    CALLING("calling", "正在调用"),

    /**
     * s u c c e s s.
     */
    SUCCESS("success", "成功"),

    /**
     * f a i l e d.
     */
    FAILED("failed", "失败");

    /**
     * code.
     */
    private final String code;

    /**
     * description.
     */
    private final String description;
}
