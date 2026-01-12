package io.github.hijun.agent.service;

import io.github.hijun.agent.common.enums.ModelProvider;
import io.github.hijun.agent.entity.po.ModelProviderConfig;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;

/**
 * ChatClient 工厂接口
 * <p>
 * 用于根据模型提供商配置动态创建 ChatClient 和 ChatModel
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/01/11
 * @since 3.4.3
 */
public interface ChatClientFactory {

    /**
     * 根据提供商配置ID创建 ChatClient
     *
     * @param providerConfigId 提供商配置ID
     * @param modelId          模型ID
     * @return ChatClient
     */
    ChatClient createChatClient(String providerConfigId, String modelId);

    /**
     * 根据提供商配置创建 ChatClient
     *
     * @param config  提供商配置
     * @param modelId 模型ID
     * @return ChatClient
     */
    ChatClient createChatClient(ModelProviderConfig config, String modelId);

    /**
     * 根据提供商类型和配置创建 ChatClient
     *
     * @param providerType 提供商类型
     * @param apiKey       API Key
     * @param baseUrl      Base URL
     * @param modelId      模型ID
     * @return ChatClient
     */
    ChatClient createChatClient(ModelProvider providerType, String apiKey, String baseUrl, String modelId);

    /**
     * 根据提供商配置ID创建 ChatModel
     *
     * @param providerConfigId 提供商配置ID
     * @param modelId          模型ID
     * @return ChatModel
     */
    ChatModel createChatModel(String providerConfigId, String modelId);

    /**
     * 根据提供商配置创建 ChatModel
     *
     * @param config  提供商配置
     * @param modelId 模型ID
     * @return ChatModel
     */
    ChatModel createChatModel(ModelProviderConfig config, String modelId);

    /**
     * 根据提供商类型和配置创建 ChatModel
     *
     * @param providerType 提供商类型
     * @param apiKey       API Key
     * @param baseUrl      Base URL
     * @param modelId      模型ID
     * @return ChatModel
     */
    ChatModel createChatModel(ModelProvider providerType, String apiKey, String baseUrl, String modelId);
}
