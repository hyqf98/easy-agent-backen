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
@ConfigurationProperties(prefix = ApplicationProperties.PREFIX)
public class ApplicationProperties {

    /**
     * 默认存储路径。
     */
    public static final String DEFAULT_STORAGE_PATH = "/tmp/agent-files/";

    /**
     * prefix.
     */
    public static final String PREFIX = "agent";

    /**
     * storage path.
     */
    private String storagePath;
}
