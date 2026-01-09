package io.github.hijun.agent.entity.dto;

import io.github.hijun.agent.common.enums.ToolStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Sse Message
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:haijun@email.com"
 * @date 2025/12/24 16:53
 * @since 3.4.3
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ToolMessage extends SseMessage {

    /**
     * id.
     */
    private String id;
    /**
     * name.
     */
    private String name;

    /**
     * result.
     */
    private ToolStatus toolStatus;

    /**
     * result.
     */
    private String result;
}
