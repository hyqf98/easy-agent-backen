package io.github.hijun.agent.entity.sse;

import io.github.hijun.agent.common.enums.ToolStatus;
import lombok.Builder;
import lombok.Data;

/**
 * <p> </p>
 *
 * @author haijun
 * @date 2026.02.02 10:36
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 */
@Data
@Builder
public class ToolCallMessage {

    /**
     * id.
     */
    private String id;

    /**
     * type.
     */
    private String type;

    /**
     * status.
     */
    private ToolStatus status;

    /**
     * name.
     */
    private String name;

    /**
     * params.
     */
    private String params;

    /**
     * result.
     */
    private String result;


    /**
     * error message.
     */
    private String errorMessage;
}
