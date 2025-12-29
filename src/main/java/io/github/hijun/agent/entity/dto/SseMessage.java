package io.github.hijun.agent.entity.dto;

import io.github.hijun.agent.common.enums.SseMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sse Message
 *
 * @author haijun
 * @email "mailto:haijun@email.com"
 * @date 2025/12/24 16:53
 * @version 3.4.3
 * @since 3.4.3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SseMessage {
    /**
     * 消息类型
     */
    private SseMessageType type;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 消息内容
     */
    private Object content;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 是否为最后一条消息
     */
    @Builder.Default
    private Boolean finished = false;
}
