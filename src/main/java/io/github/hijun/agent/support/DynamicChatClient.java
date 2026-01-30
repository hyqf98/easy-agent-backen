package io.github.hijun.agent.support;

import io.github.hijun.agent.common.enums.ModelProvider;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;

/**
 * 动态 ChatClient 工具类
 * <p>
 * 支持动态选择 ChatModel 的 ChatClient 创建工具，配合 ChatModelFactory 使用
 * 简化了每次请求时手动创建 ChatClient 的流程
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
public class DynamicChatClient {

    /**
     * ChatModel 工厂
     */
    private final ChatModelFactory chatModelFactory;

    /**
     * 模型配置属性
     */
    private final ModelProperties modelProperties;

    /**
     * 创建动态 ChatClient
     *
     * @param chatModelFactory ChatModel 工厂
     * @param modelProperties  模型配置属性
     * @since 1.0.0-SNAPSHOT
     */
    public DynamicChatClient(ChatModelFactory chatModelFactory, ModelProperties modelProperties) {
        this.chatModelFactory = chatModelFactory;
        this.modelProperties = modelProperties;
    }

    /**
     * 创建 ChatClient（使用默认提供商）
     *
     * @return ChatClient 实例
     * @since 1.0.0-SNAPSHOT
     */
    public ChatClient create() {
        return this.create(this.modelProperties.getDefaultProvider());
    }

    /**
     * 创建 ChatClient（使用指定提供商）
     *
     * @param provider 模型提供商
     * @return ChatClient 实例
     * @since 1.0.0-SNAPSHOT
     */
    public ChatClient create(ModelProvider provider) {
        ChatModel chatModel = this.chatModelFactory.getChatModel(provider);
        return ChatClient.builder(chatModel).build();
    }

    /**
     * 根据 ChatForm 创建 ChatClient
     *
     * @param provider 提供商字符串（可为null，使用默认值）
     * @return ChatClient 实例
     * @since 1.0.0-SNAPSHOT
     */
    public ChatClient createFromProvider(ModelProvider provider) {
        ModelProvider actualProvider = (provider != null) ? provider : this.modelProperties.getDefaultProvider();
        return this.create(actualProvider);
    }

    /**
     * 获取 ChatModel
     *
     * @param provider 模型提供商
     * @return ChatModel 实例
     * @since 1.0.0-SNAPSHOT
     */
    public ChatModel getChatModel(String provider) {
        return this.chatModelFactory.getChatModel(provider);
    }

    /**
     * 获取默认 ChatModel
     *
     * @return ChatModel 实例
     * @since 1.0.0-SNAPSHOT
     */
    public ChatModel getDefaultChatModel() {
        return this.chatModelFactory.getChatModel(this.modelProperties.getDefaultProvider());
    }
}

