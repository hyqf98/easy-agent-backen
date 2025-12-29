package io.github.hijun.agent.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Ai Config
 *
 * @author haijun
 * @email "mailto:haijun@email.com"
 * @date 2025/12/24 17:32
 * @version 3.4.3
 * @since 3.4.3
 */
@Configuration
public class SpringAiConfig {

    /**
     * Create ChatClient.Builder bean
     *
     * @param chatModel OpenAI chat model (auto-configured from application.yml)
     * @return ChatClient.Builder
     * @since 3.4.3
     */
    @Bean
    public ChatClient.Builder chatClientBuilder(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }
}
