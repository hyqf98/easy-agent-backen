package io.github.hijun.agent.controller;

import io.github.hijun.agent.entity.dto.McpServerInfoDTO;
import io.github.hijun.agent.entity.dto.McpTestResultDTO;
import io.github.hijun.agent.entity.dto.ProviderInfoDTO;
import io.github.hijun.agent.entity.dto.SettingsConfigDTO;
import io.github.hijun.agent.entity.dto.TestMcpServerDTO;
import io.github.hijun.agent.entity.dto.TestModelResultDTO;
import io.github.hijun.agent.entity.req.SaveConfigRequest;
import io.github.hijun.agent.entity.req.TestModelRequest;
import io.github.hijun.agent.service.McpClientService;
import io.github.hijun.agent.service.SettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 设置控制器
 * <p>
 * 提供系统设置相关的 REST API 接口
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/01/11
 * @since 3.4.3
 */
@Slf4j
// @RestController
// @RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    /**
     * 设置服务
     */
    private final SettingsService settingsService;

    /**
     * MCP 客户端服务
     */
    private final McpClientService mcpClientService;

    /**
     * 获取所有配置
     * <p>
     * 查询并返回系统的所有配置信息，包括 MCP 服务器列表和模型提供商列表
     *
     * @return 系统配置信息 DTO
     * @since 1.0.0-SNAPSHOT
     */
    @GetMapping("/config")
    public SettingsConfigDTO getConfig() {
        return this.settingsService.getConfig();
    }

    /**
     * 保存配置
     * <p>
     * 保存 MCP 服务器配置和模型提供商配置到数据库
     *
     * @param request 配置保存请求，包含 MCP 服务器列表和模型提供商列表
     * @since 1.0.0-SNAPSHOT
     */
    @PostMapping("/config")
    public void saveConfig(@Valid @RequestBody SaveConfigRequest request) {
        this.settingsService.saveConfig(request);
    }

    /**
     * 获取支持的模型提供商列表
     * <p>
     * 返回系统支持的所有模型提供商信息，包括提供商名称、是否需要 API Key 等元数据
     *
     * @return 模型提供商信息列表
     * @since 1.0.0-SNAPSHOT
     */
    @GetMapping("/providers")
    public List<ProviderInfoDTO> getProviders() {
        return this.settingsService.getProviders();
    }

    /**
     * 测试模型连接
     * <p>
     * 测试指定模型的连接是否可用，返回测试结果和延迟信息
     *
     * @param request 测试请求，包含提供商类型、模型 ID、API Key 等信息
     * @return 测试结果 DTO，包含是否成功、消息和延迟时间
     * @since 1.0.0-SNAPSHOT
     */
    @PostMapping("/test-model")
    public TestModelResultDTO testModel(@Valid @RequestBody TestModelRequest request) {
        return this.settingsService.testModel(request);
    }

    /**
     * 测试 MCP 服务器连接
     * <p>
     * 测试指定 MCP 服务器的连接是否可用，并获取服务器信息
     *
     * @param request 测试请求，包含服务器地址和传输模式
     * @return 测试结果 DTO，包含是否成功、消息、延迟和服务器信息
     * @since 1.0.0-SNAPSHOT
     */
    @PostMapping("/test-mcp")
    public McpTestResultDTO testMcpServer(@Valid @RequestBody TestMcpServerDTO request) {
        return this.mcpClientService.testConnection(request);
    }

    /**
     * 获取 MCP 服务器信息
     * <p>
     * 连接到指定的 MCP 服务器并获取详细的服务器信息
     *
     * @param serverUrl 服务器地址
     * @param transportMode 传输模式 (sse 或 http_stream)
     * @return MCP 服务器信息 DTO
     * @since 1.0.0-SNAPSHOT
     */
    @GetMapping("/mcp-info")
    public McpServerInfoDTO getMcpServerInfo(
            @RequestParam String serverUrl,
            @RequestParam String transportMode) {
        return this.mcpClientService.getServerInfo(serverUrl, transportMode);
    }
}
