package io.github.hijun.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 文件配置属性
 *
 * @author haijun
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/1/7 11:00
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = AgentProperties.PREFIX)
public class AgentProperties {

    /**
     * prefix.
     */
    public static final String PREFIX = "agent";

    /**
     * storage path.
     */
    private String storagePath;
}
