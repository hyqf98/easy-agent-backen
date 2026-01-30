package io.github.hijun.agent.support;

import io.github.hijun.agent.common.enums.ModelProvider;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 模型配置属性
 * <p>
 * 从配置文件读取各个模型的API密钥和配置
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/1/30 18:56
 */
@Data
@Component
@ConfigurationProperties(prefix = "agent.model")
public class ModelProperties {

    /**
     * OpenAI 配置
     */
    private OpenAIConfig openai = new OpenAIConfig();

    /**
     * 智谱AI 配置
     */
    private ZhiPuAIConfig zhipuai = new ZhiPuAIConfig();

    /**
     * Anthropic 配置
     */
    private AnthropicConfig anthropic = new AnthropicConfig();

    /**
     * 默认模型提供商
     */
    private ModelProvider defaultProvider = ModelProvider.OPENAI;

    /**
     * OpenAI 配置
     *
     * @author haijun
     * @email "mailto:iamxiaohaijun@gmail.com"
     * @date 2026/1/30 18:56
     * @version 1.0.0-SNAPSHOT
     * @since 1.0.0-SNAPSHOT
     */
    @Data
    public static class OpenAIConfig {
        /**
         * API 密钥
         */
        private String apiKey;

        /**
         * 基础URL
         */
        private String baseUrl = "https://api.openai.com";

        /**
         * 默认模型
         */
        private String model = "gpt-4o";

        /**
         * 默认温度
         */
        private Double temperature = 0.7;

        /**
         * 默认最大Token数
         */
        private Integer maxTokens = 2000;
    }

    /**
     * 智谱AI 配置
     *
     * @author haijun
     * @email "mailto:iamxiaohaijun@gmail.com"
     * @date 2026/1/30 18:56
     * @version 1.0.0-SNAPSHOT
     * @since 1.0.0-SNAPSHOT
     */
    @Data
    public static class ZhiPuAIConfig {
        /**
         * API 密钥
         */
        private String apiKey;

        /**
         * 基础URL（当前未使用，保留以备将来扩展）
         * 注意：ZhipuAiApi构造函数只接受apiKey，不支持自定义baseUrl
         * 如需使用国际平台 Z.ai，需要通过其他方式配置
         */
        private String baseUrl = "https://open.bigmodel.cn/api/paas";

        /**
         * 默认模型
         */
        private String model = "glm-4-air";

        /**
         * 默认温度
         */
        private Double temperature = 0.7;

        /**
         * 默认最大Token数
         */
        private Integer maxTokens = 2000;
    }

    /**
     * Anthropic 配置
     *
     * @author haijun
     * @email "mailto:iamxiaohaijun@gmail.com"
     * @date 2026/1/30 18:56
     * @version 1.0.0-SNAPSHOT
     * @since 1.0.0-SNAPSHOT
     */
    @Data
    public static class AnthropicConfig {
        /**
         * API 密钥
         */
        private String apiKey;

        /**
         * 基础URL（当前未使用，保留以备将来扩展）
         * 注意：AnthropicApi构造函数只接受apiKey，不支持自定义baseUrl
         */
        private String baseUrl = "api.anthropic.com";

        /**
         * API版本（当前未使用，保留以备将来扩展）
         * 注意：AnthropicApi使用默认版本，不支持在构造函数中自定义版本
         */
        private String version = "2023-06-01";

        /**
         * 默认模型
         */
        private String model = "claude-sonnet-4-5";

        /**
         * 默认温度
         */
        private Double temperature = 0.7;

        /**
         * 默认最大Token数
         */
        private Integer maxTokens = 2000;
    }
}
