package io.github.hijun.agent.config;

import io.github.hijun.agent.support.ChatModelFactory;
import io.github.hijun.agent.support.DynamicChatClient;
import io.github.hijun.agent.support.ModelProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI 自动配置类
 * <p>
 * 启用自定义模型配置属性，注册 ChatModelFactory 和 DynamicChatClient Bean
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Configuration
@EnableConfigurationProperties({
        ModelProperties.class,
        AgentProperties.class
})
public class SpringAiAutoConfiguration {

    /**
     * 创建 DynamicChatClient Bean
     *
     * @param chatModelFactory ChatModel 工厂
     * @param modelProperties  模型配置属性
     * @return DynamicChatClient 实例
     * @since 1.0.0-SNAPSHOT
     */
    @Bean
    public DynamicChatClient dynamicChatClient(ChatModelFactory chatModelFactory,
                                               ModelProperties modelProperties) {
        return new DynamicChatClient(chatModelFactory, modelProperties);
    }
}
