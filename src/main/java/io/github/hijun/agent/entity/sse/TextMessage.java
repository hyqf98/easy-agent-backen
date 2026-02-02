package io.github.hijun.agent.entity.sse;

import lombok.Builder;
import lombok.Data;

/**
 * <p> </p>
 *
 * @author haijun
 * @date 2026.02.02 10:38
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Data
@Builder
public class TextMessage {

    /**
     * model name.
     */
    private String modelName;

    /**
     * text.
     */
    private String text;

}
