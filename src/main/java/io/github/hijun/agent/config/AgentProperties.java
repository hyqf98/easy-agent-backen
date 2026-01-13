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
@ConfigurationProperties(prefix = AgentProperties.PREFIX)
public class AgentProperties {

    /**
     * p r e f i x.
     */
    public static final String PREFIX = "agent.prompt";

    /**
     * max step.
     */
    private Integer maxStep;
}
