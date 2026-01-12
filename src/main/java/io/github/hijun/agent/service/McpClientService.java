package io.github.hijun.agent.service;

import io.github.hijun.agent.entity.dto.McpServerInfoDTO;
import io.github.hijun.agent.entity.dto.McpTestResultDTO;
import io.github.hijun.agent.entity.dto.TestMcpServerDTO;

/**
 * MCP 客户端服务接口
 * <p>
 * 提供 MCP 客户端相关的业务逻辑处理，包括测试连接和获取服务器信息
 *
 * @author haijun
 * @version 3.4.3
 * @date 2026/01/12
 * @since 3.4.3
 */
public interface McpClientService {

    /**
     * 测试 MCP 服务器连接
     * <p>
     * 尝试连接到指定的 MCP 服务器并获取服务器信息
     *
     * @param request 测试请求信息
     * @return 测试结果
     */
    McpTestResultDTO testConnection(TestMcpServerDTO request);

    /**
     * 获取 MCP 服务器信息
     * <p>
     * 连接到指定的 MCP 服务器并获取详细的服务器信息
     *
     * @param serverUrl 服务器地址
     * @param transportMode 传输模式
     * @return 服务器信息
     */
    McpServerInfoDTO getServerInfo(String serverUrl, String transportMode);
}
