package io.github.hijun.agent.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Agent Status
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:haijun@email.com"
 * @date 2025/12/31 17:59
 * @since 3.4.3
 */
@Getter
@AllArgsConstructor
public enum AgentStatus {

    /**
     * i d l e.
     */
    IDLE(1, "空闲状态"),

    /**
     * r u n n i n g.
     */
    RUNNING(2, "运行状态"),

    /**
     * f i n i s h e d.
     */
    FINISHED(3, "完成状态"),

    /**
     * e r r o r.
     */
    ERROR(4, "错误状态");


    /**
     * code.
     */
    private final Integer code;
    /**
     * description.
     */
    private final String description;
}
