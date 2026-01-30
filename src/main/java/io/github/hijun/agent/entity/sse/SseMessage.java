package io.github.hijun.agent.entity.sse;

import io.github.hijun.agent.common.enums.SseMessageType;
import lombok.Builder;
import lombok.Data;

/**
 * <p> </p>
 *
 * @author haijun
 * @date 2026.01.30 19:32
 * @param <T> t
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 */
@Data
@Builder
public class SseMessage<T> {

    /**
     * session id.
     */
    private String sessionId;

    /**
     * request id.
     */
    private String requestId;

    /**
     * type.
     */
    private SseMessageType type;


    /**
     * content.
     */
    private T content;
}
