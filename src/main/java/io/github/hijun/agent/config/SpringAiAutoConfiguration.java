package io.github.hijun.agent.config;

import io.github.hijun.agent.service.ChatClientFactory;
import io.github.hijun.agent.service.impl.DynamicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
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
 * @email "mailto:haijun@email.com"
 * @date 2025/12/24 17:32
 * @since 3.4.3
 */
@Configuration
@EnableConfigurationProperties(SystemPromptProperties.class)
public class SpringAiAutoConfiguration {

    /**
     * 创建默认 ChatModel Bean
     * <p>
     * 当用户没有指定模型提供商时，使用此默认模型
     *
     * @param openAiChatModel 从 application.yml 自动配置的 OpenAI ChatModel
     * @return 默认 ChatModel
     */
    @Bean
    @ConditionalOnMissingBean(name = "defaultChatModel")
    public ChatModel defaultChatModel(OpenAiChatModel openAiChatModel) {
        return openAiChatModel;
    }

    /**
     * 创建动态 ChatModel Bean
     * <p>
     * 支持根据请求上下文动态切换不同的模型提供商
     *
     * @param defaultChatModel  默认 ChatModel
     * @param chatClientFactory ChatClient 工厂
     * @return 动态 ChatModel
     */
    @Bean
    public DynamicChatModel dynamicChatModel(ChatModel defaultChatModel, ChatClientFactory chatClientFactory) {
        return new DynamicChatModel(defaultChatModel, chatClientFactory);
    }

    /**
     * 创建 ChatClient Bean
     * <p>
     * 使用动态 ChatModel 构建 ChatClient
     *
     * @param dynamicChatModel 动态 ChatModel
     * @return ChatClient
     */
    @Bean
    public ChatClient chatClient(DynamicChatModel dynamicChatModel) {
        return ChatClient.builder(dynamicChatModel).build();
    }
}
