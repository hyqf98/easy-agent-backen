package io.github.hijun.agent.service.impl;

import cn.hutool.core.util.StrUtil;
import io.github.hijun.agent.common.enums.ModelProvider;
import io.github.hijun.agent.entity.po.ModelProviderConfig;
import io.github.hijun.agent.mapper.ModelProviderConfigMapper;
import io.github.hijun.agent.service.ChatClientFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;

/**
 * ChatClient 工厂实现
 * <p>
 * 根据模型提供商配置动态创建 ChatClient 和 ChatModel
 * <p>
 * 所有提供商统一使用 OpenAI 兼容接口，通过不同的 baseUrl 区分
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/01/11
 * @since 3.4.3
 */
@Slf4j
@RequiredArgsConstructor
public class ChatClientFactoryImpl implements ChatClientFactory {

    /**
     * 模型提供商配置 Mapper
     */
    private final ModelProviderConfigMapper modelProviderConfigMapper;

    /**
     * 默认 API 超时时间（毫秒）
     */
    private static final long DEFAULT_API_TIMEOUT = 60000L;

    /**
     * Create Chat Client
     *
     * @param providerConfigId provider config id
     * @param modelId          model id
     * @return chat client
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public ChatClient createChatClient(String providerConfigId, String modelId) {
        ChatModel chatModel = this.createChatModel(providerConfigId, modelId);
        return ChatClient.builder(chatModel).build();
    }

    /**
     * Create Chat Client
     *
     * @param config  config
     * @param modelId model id
     * @return chat client
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public ChatClient createChatClient(ModelProviderConfig config, String modelId) {
        ChatModel chatModel = this.createChatModel(config, modelId);
        return ChatClient.builder(chatModel).build();
    }

    /**
     * Create Chat Client
     *
     * @param providerType provider type
     * @param apiKey       api key
     * @param baseUrl      base url
     * @param modelId      model id
     * @return chat client
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public ChatClient createChatClient(ModelProvider providerType, String apiKey, String baseUrl, String modelId) {
        ChatModel chatModel = this.createChatModel(providerType, apiKey, baseUrl, modelId);
        return ChatClient.builder(chatModel).build();
    }

    /**
     * Create Chat Model
     *
     * @param providerConfigId provider config id
     * @param modelId          model id
     * @return chat model
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public ChatModel createChatModel(String providerConfigId, String modelId) {
        ModelProviderConfig config = this.modelProviderConfigMapper.selectById(providerConfigId);
        if (config == null) {
            throw new IllegalArgumentException("Model provider config not found: " + providerConfigId);
        }
        return this.createChatModel(config, modelId);
    }

    /**
     * Create Chat Model
     *
     * @param config  config
     * @param modelId model id
     * @return chat model
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public ChatModel createChatModel(ModelProviderConfig config, String modelId) {
        return this.createChatModel(config.getProviderType(), config.getApiKey(), config.getBaseUrl(), modelId);
    }

    /**
     * Create Chat Model
     *
     * @param providerType provider type
     * @param apiKey       api key
     * @param baseUrl      base url
     * @param modelId      model id
     * @return chat model
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public ChatModel createChatModel(ModelProvider providerType, String apiKey, String baseUrl, String modelId) {
        String resolvedBaseUrl = this.resolveBaseUrl(providerType, baseUrl);

        log.debug("Creating ChatModel: provider={}, model={}, baseUrl={}",
                providerType, modelId, resolvedBaseUrl);

        return null;
    }

    /**
     * 解析 Base URL
     * <p>
     * 如果用户未提供 baseUrl，则使用各提供商的默认地址
     *
     * @param providerType 提供商类型
     * @param baseUrl      用户配置的 baseUrl（可为空）
     * @return 解析后的 baseUrl
     * @since 1.0.0-SNAPSHOT
     */
    private String resolveBaseUrl(ModelProvider providerType, String baseUrl) {
        if (StrUtil.isNotBlank(baseUrl)) {
            return baseUrl;
        }

        return switch (providerType) {
            case OPENAI -> "https://api.openai.com/v1";
            case AZURE_OPENAI -> null;
            case HUGGINGFACE -> null;
            case MINIMAX -> null;
            case MOONSHOT -> "https://api.moonshot.cn/v1";
            case ZHIPU -> "https://open.bigmodel.cn/api/paas/v4";
            case ANTHROPIC -> "https://api.anthropic.com/v1";
            case OLLAMA -> null;
        };
    }
}
