package io.github.hijun.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * System Prompt Config
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/1/5 15:44
 * @since 1.0.0-SNAPSHOT
 */
@Data
@ConfigurationProperties(prefix = SystemPromptProperties.PREFIX)
public class SystemPromptProperties {

    /**
     * p r e f i x.
     */
    public static final String PREFIX = "agent.prompt";

    /**
     * react.
     */
    private AgentConfig react;

    /**
     * summary.
     */
    private AgentConfig summary;

    /**
     * Agent Config
     *
     * @author haijun
     * @email "mailto:iamxiaohaijun@gmail.com"
     * @date 2026/1/5 15:47
     * @version 1.0.0-SNAPSHOT
     * @since 1.0.0-SNAPSHOT
     */
    @Data
    public static class AgentConfig {
        /**
         * system prompt.
         */
        private String systemPrompt;
        /**
         * next step prompt.
         */
        private String nextStepPrompt;
        /**
         * max step.
         */
        private Integer maxStep;
    }
}
