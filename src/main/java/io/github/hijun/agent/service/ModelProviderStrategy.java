package io.github.hijun.agent.service;

import io.github.hijun.agent.entity.req.TestModelRequest;
import io.github.hijun.agent.entity.dto.TestModelResultDTO;

/**
 * 模型提供商策略接口
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:haijun@email.com"
 * @date 2026/01/11
 * @since 3.4.3
 */
public interface ModelProviderStrategy {

    /**
     * 测试模型连接
     *
     * @param request 测试请求
     * @throws Exception 连接异常
     */
    void testConnection(TestModelRequest request) throws Exception;
}
