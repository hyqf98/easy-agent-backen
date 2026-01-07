package io.github.hijun.agent.entity.dto;

import io.github.hijun.agent.common.enums.SseMessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * Session
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:zhonghaijun@zhxx.com"
 * @date 2025/12/30 11:01
 * @since 3.4.3
 */
@Data
@SuperBuilder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public abstract class SseMessage implements Serializable {

    /**
     * type.
     */
    private SseMessageType type;
}
