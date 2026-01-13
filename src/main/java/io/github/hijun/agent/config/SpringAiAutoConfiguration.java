package io.github.hijun.agent.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI 自动配置类
 * <p>
 * 配置 ChatClient 和相关 Bean，支持动态模型切换
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2025/12/24 17:32
 * @since 3.4.3
 */
@Configuration
@EnableConfigurationProperties(AgentProperties.class)
public class SpringAiAutoConfiguration {

    /**
     * 创建默认 ChatModel Bean
     * <p>
     * 当用户没有指定模型提供商时，使用此默认模型
     *
     * @param openAiChatModel 从 application.yml 自动配置的 OpenAI ChatModel
     * @return 默认 ChatModel
     * @since 1.0.0-SNAPSHOT
     */
    @Bean
    @ConditionalOnMissingBean(name = "openAiChatModel")
    public ChatClient chatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel).build();
    }
}
