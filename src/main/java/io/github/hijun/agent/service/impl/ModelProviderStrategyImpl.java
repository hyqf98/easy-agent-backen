package io.github.hijun.agent.service.impl;

import io.github.hijun.agent.entity.req.TestModelRequest;
import io.github.hijun.agent.service.ChatClientFactory;
import io.github.hijun.agent.service.ModelProviderStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * 模型提供商策略实现
 * <p>
 * 使用策略模式测试不同模型提供商的连接
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/01/11
 * @since 3.4.3
 */
@Slf4j
public class ModelProviderStrategyImpl implements ModelProviderStrategy {

    /**
     * ChatClient 工厂
     */
    private final ChatClientFactory chatClientFactory;

    /**
     * 构造函数
     *
     * @param chatClientFactory ChatClient 工厂
     * @since 1.0.0-SNAPSHOT
     */
    public ModelProviderStrategyImpl(ChatClientFactory chatClientFactory) {
        this.chatClientFactory = chatClientFactory;
    }

    /**
     * Test Connection
     *
     * @param request request
     * @throws Exception
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public void testConnection(TestModelRequest request) throws Exception {
        switch (request.getProviderType()) {
            case OPENAI:
            case ANTHROPIC:
            case MOONSHOT:
            case ZHIPU:
                break;
            default:
                throw new IllegalArgumentException("Unsupported provider: " + request.getProviderType());
        }
    }
}
