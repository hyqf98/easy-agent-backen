package io.github.hijun.agent.support;

import io.github.hijun.agent.common.enums.BaseEnum;
import io.github.hijun.agent.common.enums.ModelProvider;
import io.github.hijun.agent.entity.dto.LlmModelDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.stereotype.Component;

/**
 * ChatModel 工厂类
 * <p>
 * 根据模型提供商或数据库配置动态创建对应的 ChatModel 实例
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatModelFactory {

    /**
     * 模型配置属性（保留用于兼容默认配置）
     */
    private final ModelProperties modelProperties;

    /**
     * 根据模型提供商获取对应的 ChatModel（使用配置文件）
     *
     * @param provider 模型提供商
     * @return ChatModel 实例
     * @since 1.0.0-SNAPSHOT
     */
    public ChatModel getChatModel(ModelProvider provider) {
        return switch (provider) {
            case OPENAI -> this.createOpenAIChatModel();
            case ZHIPU_AI -> this.createZhipuAIChatModel();
            case ANTHROPIC -> this.createAnthropicChatModel();
        };
    }

    /**
     * 根据数据库模型配置创建 ChatModel
     * <p>
     * 从数据库查询的 LlmModelDTO 创建对应的 ChatModel 实例
     *
     * @param modelConfig 模型配置DTO
     * @return ChatModel 实例
     * @since 1.0.0-SNAPSHOT
     */
    public ChatModel createChatModel(LlmModelDTO modelConfig) {
        if (modelConfig == null) {
            throw new IllegalArgumentException("模型配置不能为空");
        }

        ModelProvider providerType = modelConfig.getProviderType();
        if (providerType == null) {
            throw new IllegalArgumentException("提供商类型不能为空");
        }

        log.info("从数据库配置创建 ChatModel: provider={}, modelCode={}", providerType, modelConfig.getModelCode());

        return switch (providerType) {
            case OPENAI -> this.createOpenAIChatModel(modelConfig);
            case ZHIPU_AI -> this.createZhipuAIChatModel(modelConfig);
            case ANTHROPIC -> this.createAnthropicChatModel(modelConfig);
        };
    }

    /**
     * 创建 OpenAI ChatModel（从数据库配置）
     *
     * @param modelConfig 模型配置
     * @return OpenAI ChatModel 实例
     * @since 1.0.0-SNAPSHOT
     */
    private ChatModel createOpenAIChatModel(LlmModelDTO modelConfig) {
        log.info("创建 OpenAI ChatModel: modelCode={}", modelConfig.getModelCode());

        OpenAiApi.Builder apiBuilder = OpenAiApi.builder()
                .apiKey(modelConfig.getApiKey());

        // 如果配置了 baseUrl，使用自定义地址
        if (modelConfig.getBaseUrl() != null && !modelConfig.getBaseUrl().isBlank()) {
            apiBuilder.baseUrl(modelConfig.getBaseUrl());
        }

        OpenAiApi openAiApi = apiBuilder.build();

        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder()
                .model(modelConfig.getModelCode())
                .temperature(modelConfig.getTemperature())
                .maxTokens(modelConfig.getMaxTokens());

        // 如果配置了 topP，添加到选项
        if (modelConfig.getTopP() != null) {
            optionsBuilder.topP(modelConfig.getTopP());
        }

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(optionsBuilder.build())
                .build();
    }

    /**
     * 创建智谱AI ChatModel（从数据库配置）
     *
     * @param modelConfig 模型配置
     * @return 智谱AI ChatModel 实例
     * @since 1.0.0-SNAPSHOT
     */
    private ChatModel createZhipuAIChatModel(LlmModelDTO modelConfig) {
        log.info("创建智谱AI ChatModel: modelCode={}", modelConfig.getModelCode());

        ZhiPuAiApi.Builder apiBuilder = ZhiPuAiApi.builder()
                .apiKey(modelConfig.getApiKey());

        // 智谱AI的 API 构建器支持自定义 baseUrl
        if (modelConfig.getBaseUrl() != null && !modelConfig.getBaseUrl().isBlank()) {
            apiBuilder.baseUrl(modelConfig.getBaseUrl());
        }

        ZhiPuAiApi zhiPuAiApi = apiBuilder.build();

        ZhiPuAiChatOptions.Builder optionsBuilder = ZhiPuAiChatOptions.builder()
                .model(modelConfig.getModelCode())
                .temperature(modelConfig.getTemperature())
                .maxTokens(modelConfig.getMaxTokens());

        // 如果配置了 topP，添加到选项
        if (modelConfig.getTopP() != null) {
            optionsBuilder.topP(modelConfig.getTopP());
        }

        return new ZhiPuAiChatModel(zhiPuAiApi, optionsBuilder.build());
    }

    /**
     * 创建 Anthropic ChatModel（从数据库配置）
     *
     * @param modelConfig 模型配置
     * @return Anthropic ChatModel 实例
     * @since 1.0.0-SNAPSHOT
     */
    private ChatModel createAnthropicChatModel(LlmModelDTO modelConfig) {
        log.info("创建 Anthropic ChatModel: modelCode={}", modelConfig.getModelCode());

        AnthropicApi.Builder apiBuilder = AnthropicApi.builder()
                .apiKey(modelConfig.getApiKey());

        // Anthropic 的 API 构建器支持自定义 baseUrl
        if (modelConfig.getBaseUrl() != null && !modelConfig.getBaseUrl().isBlank()) {
            apiBuilder.baseUrl(modelConfig.getBaseUrl());
        }

        AnthropicApi anthropicApi = apiBuilder.build();

        AnthropicChatOptions.Builder optionsBuilder = AnthropicChatOptions.builder()
                .model(modelConfig.getModelCode())
                .temperature(modelConfig.getTemperature())
                .maxTokens(modelConfig.getMaxTokens());

        // 如果配置了 topP，添加到选项
        if (modelConfig.getTopP() != null) {
            optionsBuilder.topP(modelConfig.getTopP());
        }

        return AnthropicChatModel.builder()
                .anthropicApi(anthropicApi)
                .defaultOptions(optionsBuilder.build())
                .build();
    }

    /**
     * 根据提供商字符串获取对应的 ChatModel
     *
     * @param provider 提供商字符串
     * @return ChatModel 实例
     * @since 1.0.0-SNAPSHOT
     */
    public ChatModel getChatModel(String provider) {
        ModelProvider modelProvider = BaseEnum.fromValue(ModelProvider.class, provider);
        if (modelProvider == null) {
            log.warn("未知的模型提供商: {}, 使用默认提供商", provider);
            modelProvider = BaseEnum.fromValue(ModelProvider.class,
                    this.modelProperties.getDefaultProvider());
            if (modelProvider == null) {
                modelProvider = ModelProvider.OPENAI;
            }
        }
        return this.getChatModel(modelProvider);
    }

    /**
     * 创建 OpenAI ChatModel
     *
     * @return OpenAI ChatModel 实例
     * @since 1.0.0-SNAPSHOT
     */
    private ChatModel createOpenAIChatModel() {
        log.info("创建 OpenAI ChatModel");

        ModelProperties.OpenAIConfig config = this.modelProperties.getOpenai();

        OpenAiApi openAiApi = OpenAiApi.builder()
                .apiKey(config.getApiKey())
                .baseUrl(config.getBaseUrl())
                .build();

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(config.getModel())
                .temperature(config.getTemperature())
                .maxTokens(config.getMaxTokens())
                .build();

        // 使用 Builder 模式创建 ChatModel，避免构造函数参数问题
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();
    }

    /**
     * 创建智谱AI ChatModel
     *
     * @return 智谱AI ChatModel 实例
     * @since 1.0.0-SNAPSHOT
     */
    private ChatModel createZhipuAIChatModel() {
        log.info("创建智谱AI ChatModel");

        ModelProperties.ZhiPuAIConfig config = this.modelProperties.getZhipuai();

        ZhiPuAiChatOptions options = ZhiPuAiChatOptions.builder()
                .model(config.getModel())
                .temperature(config.getTemperature())
                .maxTokens(config.getMaxTokens())
                .build();

        // 使用 ApiKey 接口创建
        ZhiPuAiApi zhiPuAiApi = ZhiPuAiApi.builder().apiKey(config.getApiKey()).build();

        return new ZhiPuAiChatModel(zhiPuAiApi, options);
    }

    /**
     * 创建 Anthropic ChatModel
     * <p>
     * 由于 AnthropicApi 构造函数是 private，使用 ApiKey 接口
     *
     * @return Anthropic ChatModel 实例
     * @since 1.0.0-SNAPSHOT
     */
    private ChatModel createAnthropicChatModel() {
        log.info("创建 Anthropic ChatModel");

        ModelProperties.AnthropicConfig config = this.modelProperties.getAnthropic();

        AnthropicChatOptions options = AnthropicChatOptions.builder()
                .model(config.getModel())
                .temperature(config.getTemperature())
                .maxTokens(config.getMaxTokens())
                .build();

        AnthropicApi anthropicApi = AnthropicApi.builder().apiKey(config.getApiKey()).build();

        return AnthropicChatModel.builder()
                .anthropicApi(anthropicApi)
                .defaultOptions(options)
                .build();
    }
}
