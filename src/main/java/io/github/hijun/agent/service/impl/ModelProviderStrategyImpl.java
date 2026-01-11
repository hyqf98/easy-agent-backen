package io.github.hijun.agent.service.impl;

import cn.hutool.core.util.StrUtil;
import io.github.hijun.agent.common.enums.ModelProvider;
import io.github.hijun.agent.entity.req.TestModelRequest;
import io.github.hijun.agent.service.ModelProviderStrategy;
import io.github.hijun.agent.service.ChatClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

/**
 * 模型提供商策略实现
 * <p>
 * 使用策略模式测试不同模型提供商的连接
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:haijun@email.com"
 * @date 2026/01/11
 * @since 3.4.3
 */
@Slf4j
@Service
public class ModelProviderStrategyImpl implements ModelProviderStrategy {

    /**
     * ChatClient 工厂
     */
    private final ChatClientFactory chatClientFactory;

    /**
     * 构造函数
     *
     * @param chatClientFactory ChatClient 工厂
     */
    public ModelProviderStrategyImpl(ChatClientFactory chatClientFactory) {
        this.chatClientFactory = chatClientFactory;
    }

    @Override
    public void testConnection(TestModelRequest request) throws Exception {
        switch (request.getProviderType()) {
            case OPENAI:
            case ANTHROPIC:
            case GOOGLE:
            case MOONSHOT:
            case DEEPSEEK:
            case BAIDU:
            case ALIBABA:
            case ZHIPU:
            case BYTEDANCE:
                // 所有提供商统一使用 OpenAI 兼容模式测试
                testOpenAiCompatible(request);
                break;
            default:
                throw new IllegalArgumentException("Unsupported provider: " + request.getProviderType());
        }
    }

    /**
     * 测试 OpenAI 兼容的接口
     * <p>
     * 使用 ChatClient 发送简单的测试消息，验证连接是否可用
     *
     * @param request 测试请求
     * @throws Exception 连接失败时抛出异常
     */
    private void testOpenAiCompatible(TestModelRequest request) {
        String baseUrl = request.getBaseUrl();
        if (StrUtil.isBlank(baseUrl)) {
            // 使用默认的 base url
            baseUrl = switch (request.getProviderType()) {
                case OPENAI -> "https://api.openai.com/v1";
                case ANTHROPIC -> "https://api.anthropic.com/v1";
                case GOOGLE -> "https://generativelanguage.googleapis.com/v1beta";
                case MOONSHOT -> "https://api.moonshot.cn/v1";
                case DEEPSEEK -> "https://api.deepseek.com/v1";
                case ALIBABA -> "https://dashscope.aliyuncs.com/compatible-mode/v1";
                case BAIDU -> "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat";
                case ZHIPU -> "https://open.bigmodel.cn/api/paas/v4";
                case BYTEDANCE -> "https://ark.cn-beijing.volces.com/api/v3";
                default -> throw new IllegalArgumentException("Unknown provider: " + request.getProviderType());
            };
        }

        try {
            // 使用 ChatClientFactory 创建临时的 ChatClient 进行测试
            ChatClient testClient = chatClientFactory.createChatClient(
                    request.getProviderType(),
                    request.getApiKey(),
                    baseUrl,
                    request.getModelId()
            );

            // 发送简单的测试消息
            ChatResponse response = testClient.prompt()
                    .user("test")
                    .call()
                    .chatResponse();

            if (response != null && response.getResult() != null) {
                log.info("Test connection successful: provider={}, model={}, response={}",
                        request.getProviderType(), request.getModelId(), response.getResult().getOutput().getContent());
            } else {
                throw new RuntimeException("Connection test failed: no response received");
            }
        } catch (Exception e) {
            log.error("Test connection failed: provider={}, model={}, error={}",
                    request.getProviderType(), request.getModelId(), e.getMessage());
            throw new RuntimeException("Connection test failed: " + e.getMessage(), e);
        }
    }
}
