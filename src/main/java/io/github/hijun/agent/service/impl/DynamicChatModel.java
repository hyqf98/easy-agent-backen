package io.github.hijun.agent.service.impl;

import io.github.hijun.agent.service.ChatClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态 ChatModel
 * <p>
 * 根据请求上下文动态选择不同的模型提供商
 * <p>
 * 使用 ThreadLocal 存储当前请求的模型配置，实现请求级别的模型切换
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/01/11
 * @since 3.4.3
 */
@Slf4j
public class DynamicChatModel implements ChatModel {

    /**
     * 默认 ChatModel（当未指定模型提供商时使用）
     */
    private final ChatModel defaultChatModel;

    /**
     * ChatClient 工厂
     */
    private final ChatClientFactory chatClientFactory;

    /**
     * ThreadLocal 存储当前请求的模型配置
     */
    private static final ThreadLocal<ModelConfig> CURRENT_MODEL_CONFIG = new ThreadLocal<>();

    /**
     * 缓存已创建的 ChatModel 实例
     * <p>
     * Key: providerConfigId:modelId
     */
    private final Map<String, ChatModel> chatModelCache = new ConcurrentHashMap<>();

    /**
     * 构造函数
     *
     * @param defaultChatModel   默认 ChatModel
     * @param chatClientFactory  ChatClient 工厂
     */
    public DynamicChatModel(ChatModel defaultChatModel, ChatClientFactory chatClientFactory) {
        this.defaultChatModel = defaultChatModel;
        this.chatClientFactory = chatClientFactory;
    }

    /**
     * 设置当前请求的模型配置
     *
     * @param providerConfigId 提供商配置ID
     * @param modelId          模型ID
     */
    public static void setCurrentModel(String providerConfigId, String modelId) {
        CURRENT_MODEL_CONFIG.set(new ModelConfig(providerConfigId, modelId));
    }

    /**
     * 清除当前请求的模型配置
     */
    public static void clearCurrentModel() {
        CURRENT_MODEL_CONFIG.remove();
    }

    /**
     * 获取当前请求应该使用的 ChatModel
     * <p>
     * 如果 ThreadLocal 中有配置，则使用对应的模型；否则使用默认模型
     *
     * @return ChatModel
     */
    private ChatModel getChatModel() {
        ModelConfig config = CURRENT_MODEL_CONFIG.get();
        if (config == null || config.providerConfigId() == null) {
            return this.defaultChatModel;
        }

        String cacheKey = config.providerConfigId() + ":" + config.modelId();
        return this.chatModelCache.computeIfAbsent(cacheKey, key -> {
            try {
                return this.chatClientFactory.createChatModel(config.providerConfigId(), config.modelId());
            } catch (Exception e) {
                log.error("Failed to create ChatModel for provider: {}, model: {}",
                        config.providerConfigId(), config.modelId(), e);
                return this.defaultChatModel;
            }
        });
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        return this.getChatModel().call(prompt);
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        return this.getChatModel().stream(prompt);
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return this.getChatModel().getDefaultOptions();
    }

    /**
     * 模型配置记录
     *
     * @param providerConfigId 提供商配置ID
     * @param modelId          模型ID
     */
    private record ModelConfig(String providerConfigId, String modelId) {
    }
}
