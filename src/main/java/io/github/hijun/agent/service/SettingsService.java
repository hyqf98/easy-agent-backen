package io.github.hijun.agent.service;

import io.github.hijun.agent.entity.dto.*;
import io.github.hijun.agent.entity.req.SaveConfigRequest;
import io.github.hijun.agent.entity.req.TestModelRequest;

import java.util.List;

/**
 * 设置服务接口
 * <p>
 * 提供系统设置相关的业务逻辑处理，包括 MCP 服务器配置和模型提供商配置的管理
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:haijun@email.com"
 * @date 2026/01/11
 * @since 3.4.3
 */
public interface SettingsService {

    /**
     * 获取所有配置
     * <p>
     * 查询并返回系统的所有配置信息，包括 MCP 服务器列表和模型提供商列表
     *
     * @return 系统配置信息 DTO
     */
    SettingsConfigDTO getConfig();

    /**
     * 保存配置
     * <p>
     * 保存 MCP 服务器配置和模型提供商配置到数据库
     *
     * @param request 配置保存请求，包含 MCP 服务器列表和模型提供商列表
     */
    void saveConfig(SaveConfigRequest request);

    /**
     * 获取支持的模型提供商列表
     * <p>
     * 返回系统支持的所有模型提供商信息，包括提供商名称、是否需要 API Key 等元数据
     *
     * @return 模型提供商信息列表
     */
    List<ProviderInfoDTO> getProviders();

    /**
     * 测试模型连接
     * <p>
     * 测试指定模型的连接是否可用，返回测试结果和延迟信息
     *
     * @param request 测试请求，包含提供商类型、模型 ID、API Key 等信息
     * @return 测试结果 DTO，包含是否成功、消息和延迟时间
     */
    TestModelResultDTO testModel(TestModelRequest request);
}
