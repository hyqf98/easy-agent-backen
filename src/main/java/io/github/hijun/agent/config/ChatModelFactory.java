package io.github.hijun.agent.config;

import io.github.hijun.agent.common.enums.ModelProvider;
import io.github.hijun.agent.entity.po.ModelProviderConfig;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spring AI 聊天模型工厂
 * <p>
 * 根据配置动态创建各种AI模型的ChatModel实例
 * 支持的模型提供商：OpenAI、Anthropic、Ollama、Azure OpenAI、HuggingFace、MiniMax、Moonshot、ZhiPu等
 *
 * @author haijun
 * @version 3.4.3
 * @date 2026/01/12
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @since 1.0.0-SNAPSHOT
 */
@Component
public class ChatModelFactory {

    /**
     * 模型缓存
     * <p>
     * 使用providerId作为key，缓存已创建的ChatModel实例
     */
    private final Map<String, ChatModel> modelCache = new ConcurrentHashMap<>();

    /**
     * 创建ChatModel实例
     * <p>
     * 根据提供商配置创建对应的ChatModel
     *
     * @param config 模型提供商配置
     * @return ChatModel实例
     * @since 1.0.0-SNAPSHOT
     */
    public ChatModel createChatModel(ModelProviderConfig config) {
        // 检查缓存
        String cacheKey = this.buildCacheKey(config);
        if (this.modelCache.containsKey(cacheKey)) {
            return this.modelCache.get(cacheKey);
        }

        ChatModel chatModel = this.doCreateChatModel(config);
        this.modelCache.put(cacheKey, chatModel);
        return chatModel;
    }

    /**
     * 创建ChatModel实例（内部实现）
     *
     * @param config config
     * @return chat model
     * @since 1.0.0-SNAPSHOT
     */
    private ChatModel doCreateChatModel(ModelProviderConfig config) {
        return switch (config.getProviderType()) {
            case ANTHROPIC -> null;
            case AZURE_OPENAI -> null;
            case OLLAMA -> null;
            case OPENAI, HUGGINGFACE, MINIMAX, MOONSHOT, ZHIPU -> null;
            default -> throw new IllegalArgumentException("不支持的模型提供商: " + config.getProviderType());
        };
    }

    /**
     * 获取提供商的默认 Base URL
     *
     * @param provider provider
     * @return string
     * @since 1.0.0-SNAPSHOT
     */
    private String getDefaultBaseUrl(ModelProvider provider) {
        return switch (provider) {
            case OPENAI -> "https://api.openai.com/v1";
            case HUGGINGFACE -> "https://api-inference.huggingface.co/v1";
            case MINIMAX -> "https://api.minimax.chat/v1";
            case MOONSHOT -> "https://api.moonshot.cn/v1";
            case ZHIPU -> "https://open.bigmodel.cn/api/paas/v4";
            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        };
    }

    /**
     * 获取提供商的默认模型
     *
     * @param provider provider
     * @return string
     * @since 1.0.0-SNAPSHOT
     */
    private String getDefaultModel(ModelProvider provider) {
        return switch (provider) {
            case OPENAI -> "gpt-4o-mini";
            case HUGGINGFACE -> "meta-llama/Llama-3.2-3B-Instruct";
            case MINIMAX -> "abab6.5s-chat";
            case MOONSHOT -> "moonshot-v1-8k";
            case ZHIPU -> "glm-4-flash";
            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        };
    }

    /**
     * 清除缓存
     * <p>
     * 当配置更新时调用此方法清除缓存
     *
     * @param providerId 提供商ID，如果为null则清除所有缓存
     * @since 1.0.0-SNAPSHOT
     */
    public void clearCache(String providerId) {
        if (providerId == null) {
            this.modelCache.clear();
        } else {
            // 清除特定提供商的缓存
            this.modelCache.entrySet().removeIf(entry -> entry.getKey().startsWith(providerId));
        }
    }

    /**
     * 构建缓存key
     *
     * @param config config
     * @return string
     * @since 1.0.0-SNAPSHOT
     */
    private String buildCacheKey(ModelProviderConfig config) {
        return String.format("%s_%s_%s",
                config.getId(),
                config.getProviderType(),
                config.getModelName());
    }
}
