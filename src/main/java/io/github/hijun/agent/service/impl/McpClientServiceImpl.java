package io.github.hijun.agent.service.impl;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.hijun.agent.common.enums.McpTransportMode;
import io.github.hijun.agent.entity.dto.McpServerInfoDTO;
import io.github.hijun.agent.entity.dto.McpTestResultDTO;
import io.github.hijun.agent.entity.dto.TestMcpServerDTO;
import io.github.hijun.agent.service.McpClientService;
import io.github.hijun.agent.utils.JSONS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

/**
 * MCP 客户端服务实现
 * <p>
 * 提供 MCP 客户端相关的业务逻辑处理
 *
 * @author haijun
 * @version 3.4.3
 * @date 2026/01/12
 * @since 3.4.3
 */
@Slf4j
@Service
public class McpClientServiceImpl implements McpClientService {

    private final RestClient.Builder restClientBuilder;

    public McpClientServiceImpl(RestClient.Builder restClientBuilder) {
        this.restClientBuilder = restClientBuilder;
    }

    @Override
    public McpTestResultDTO testConnection(TestMcpServerDTO request) {
        long startTime = System.currentTimeMillis();
        try {
            log.info("Testing MCP server connection: url={}, transportMode={}", 
                    request.getUrl(), request.getTransportMode());

            // 获取服务器信息
            McpServerInfoDTO serverInfo = getServerInfo(request.getUrl(), request.getTransportMode());

            long latency = System.currentTimeMillis() - startTime;

            return McpTestResultDTO.builder()
                    .success(true)
                    .message("连接成功")
                    .latency(latency)
                    .serverInfo(serverInfo)
                    .build();
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - startTime;
            log.error("Failed to test MCP server: url={}, error={}", request.getUrl(), e.getMessage(), e);
            return McpTestResultDTO.builder()
                    .success(false)
                    .message("连接失败: " + e.getMessage())
                    .latency(latency)
                    .error(e.getClass().getSimpleName() + ": " + e.getMessage())
                    .build();
        }
    }

    @Override
    public McpServerInfoDTO getServerInfo(String serverUrl, String transportMode) {
        try {
            McpTransportMode mode = McpTransportMode.fromCode(transportMode);

            if (mode == McpTransportMode.SSE) {
                return getSSEServerInfo(serverUrl);
            } else if (mode == McpTransportMode.HTTP_STREAM) {
                return getHttpStreamServerInfo(serverUrl);
            } else {
                throw new IllegalArgumentException("Unsupported transport mode: " + transportMode);
            }
        } catch (Exception e) {
            log.error("Failed to get MCP server info: url={}, error={}", serverUrl, e.getMessage(), e);
            throw new RuntimeException("Failed to get MCP server info: " + e.getMessage(), e);
        }
    }

    /**
     * 获取 SSE 传输模式的服务器信息
     */
    private McpServerInfoDTO getSSEServerInfo(String serverUrl) throws Exception {
        String baseUrl = normalizeBaseUrl(serverUrl);

        // 发送 initialize 请求
        JsonNode initResponse = sendMcpRequest(baseUrl, "initialize", 
                JSONS.getObjectMapper().createObjectNode());

        // 获取服务器信息
        JsonNode serverInfo = initResponse.path("result").path("serverInfo");
        String name = serverInfo.path("name").asText();
        String version = serverInfo.path("version").asText();
        String protocolVersion = initResponse.path("result").path("protocolVersion").asText();

        // 获取能力信息
        JsonNode capabilities = initResponse.path("result").path("capabilities");
        McpServerInfoDTO.ServerCapabilities serverCapabilities = McpServerInfoDTO.ServerCapabilities.builder()
                .tools(capabilities.path("tools").asBoolean(false))
                .resources(capabilities.path("resources").asBoolean(false))
                .prompts(capabilities.path("prompts").asBoolean(false))
                .logging(capabilities.path("logging").asBoolean(false))
                .sampling(capabilities.path("sampling").asBoolean(false))
                .build();

        // 获取工具列表
        List<McpServerInfoDTO.ToolInfo> tools = new ArrayList<>();
        if (Boolean.TRUE.equals(serverCapabilities.getTools())) {
            tools = fetchTools(baseUrl);
        }

        // 获取资源列表
        List<McpServerInfoDTO.ResourceInfo> resources = new ArrayList<>();
        if (Boolean.TRUE.equals(serverCapabilities.getResources())) {
            resources = fetchResources(baseUrl);
        }

        // 获取提示词列表
        List<McpServerInfoDTO.PromptInfo> prompts = new ArrayList<>();
        if (Boolean.TRUE.equals(serverCapabilities.getPrompts())) {
            prompts = fetchPrompts(baseUrl);
        }

        return McpServerInfoDTO.builder()
                .name(name)
                .version(version)
                .protocolVersion(protocolVersion)
                .capabilities(serverCapabilities)
                .tools(tools)
                .resources(resources)
                .prompts(prompts)
                .build();
    }

    /**
     * 获取 HTTP Stream 传输模式的服务器信息
     */
    private McpServerInfoDTO getHttpStreamServerInfo(String serverUrl) throws Exception {
        String baseUrl = normalizeBaseUrl(serverUrl);

        // 发送 initialize 请求
        JsonNode initResponse = sendMcpRequest(baseUrl, "initialize", 
                JSONS.getObjectMapper().createObjectNode());

        // 获取服务器信息（与 SSE 模式相同）
        JsonNode serverInfo = initResponse.path("result").path("serverInfo");
        String name = serverInfo.path("name").asText();
        String version = serverInfo.path("version").asText();
        String protocolVersion = initResponse.path("result").path("protocolVersion").asText();

        // 获取能力信息
        JsonNode capabilities = initResponse.path("result").path("capabilities");
        McpServerInfoDTO.ServerCapabilities serverCapabilities = McpServerInfoDTO.ServerCapabilities.builder()
                .tools(capabilities.path("tools").asBoolean(false))
                .resources(capabilities.path("resources").asBoolean(false))
                .prompts(capabilities.path("prompts").asBoolean(false))
                .logging(capabilities.path("logging").asBoolean(false))
                .sampling(capabilities.path("sampling").asBoolean(false))
                .build();

        // 获取工具列表
        List<McpServerInfoDTO.ToolInfo> tools = new ArrayList<>();
        if (Boolean.TRUE.equals(serverCapabilities.getTools())) {
            tools = fetchTools(baseUrl);
        }

        // 获取资源列表
        List<McpServerInfoDTO.ResourceInfo> resources = new ArrayList<>();
        if (Boolean.TRUE.equals(serverCapabilities.getResources())) {
            resources = fetchResources(baseUrl);
        }

        // 获取提示词列表
        List<McpServerInfoDTO.PromptInfo> prompts = new ArrayList<>();
        if (Boolean.TRUE.equals(serverCapabilities.getPrompts())) {
            prompts = fetchPrompts(baseUrl);
        }

        return McpServerInfoDTO.builder()
                .name(name)
                .version(version)
                .protocolVersion(protocolVersion)
                .capabilities(serverCapabilities)
                .tools(tools)
                .resources(resources)
                .prompts(prompts)
                .build();
    }

    /**
     * 发送 MCP 请求
     */
    private JsonNode sendMcpRequest(String baseUrl, String method, JsonNode params) throws Exception {
        RestClient client = restClientBuilder
                .baseUrl(baseUrl)
                .build();

        String requestBody = JSONS.getObjectMapper().createObjectNode()
                .put("jsonrpc", "2.0")
                .put("id", 1)
                .put("method", method)
                .set("params", params)
                .toJSON();

        ResponseEntity<String> response = client.post()
                .uri("/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .toEntity(String.class);

        return JSONS.getObjectMapper().readTree(response.getBody());
    }

    /**
     * 获取工具列表
     */
    private List<McpServerInfoDTO.ToolInfo> fetchTools(String baseUrl) throws Exception {
        JsonNode response = sendMcpRequest(baseUrl, "tools/list", 
                JSONS.getObjectMapper().createObjectNode());

        List<McpServerInfoDTO.ToolInfo> tools = new ArrayList<>();
        JsonNode toolsArray = response.path("result").path("tools");
        if (toolsArray != null && toolsArray.isArray()) {
            for (JsonNode toolNode : toolsArray) {
                tools.add(McpServerInfoDTO.ToolInfo.builder()
                        .name(toolNode.path("name").asText())
                        .description(toolNode.path("description").asText())
                        .inputSchema(toolNode.has("inputSchema") ? 
                                JSONS.getObjectMapper().readTree(toolNode.path("inputSchema").toString()) : null)
                        .build());
            }
        }
        return tools;
    }

    /**
     * 获取资源列表
     */
    private List<McpServerInfoDTO.ResourceInfo> fetchResources(String baseUrl) throws Exception {
        JsonNode response = sendMcpRequest(baseUrl, "resources/list", 
                JSONS.getObjectMapper().createObjectNode());

        List<McpServerInfoDTO.ResourceInfo> resources = new ArrayList<>();
        JsonNode resourcesArray = response.path("result").path("resources");
        if (resourcesArray != null && resourcesArray.isArray()) {
            for (JsonNode resourceNode : resourcesArray) {
                resources.add(McpServerInfoDTO.ResourceInfo.builder()
                        .uri(resourceNode.path("uri").asText())
                        .name(resourceNode.path("name").asText())
                        .description(resourceNode.has("description") ? resourceNode.path("description").asText() : "")
                        .mimeType(resourceNode.has("mimeType") ? resourceNode.path("mimeType").asText() : "text/plain")
                        .build());
            }
        }
        return resources;
    }

    /**
     * 获取提示词列表
     */
    private List<McpServerInfoDTO.PromptInfo> fetchPrompts(String baseUrl) throws Exception {
        JsonNode response = sendMcpRequest(baseUrl, "prompts/list", 
                JSONS.getObjectMapper().createObjectNode());

        List<McpServerInfoDTO.PromptInfo> prompts = new ArrayList<>();
        JsonNode promptsArray = response.path("result").path("prompts");
        if (promptsArray != null && promptsArray.isArray()) {
            for (JsonNode promptNode : promptsArray) {
                List<McpServerInfoDTO.PromptInfo.Argument> arguments = new ArrayList<>();
                JsonNode argsArray = promptNode.path("arguments");
                if (argsArray != null && argsArray.isArray()) {
                    for (JsonNode argNode : argsArray) {
                        arguments.add(McpServerInfoDTO.PromptInfo.Argument.builder()
                                .name(argNode.path("name").asText())
                                .description(argNode.has("description") ? argNode.path("description").asText() : "")
                                .required(argNode.has("required") ? argNode.path("required").asText() : "")
                                .build());
                    }
                }

                prompts.add(McpServerInfoDTO.PromptInfo.builder()
                        .name(promptNode.path("name").asText())
                        .description(promptNode.has("description") ? promptNode.path("description").asText() : "")
                        .arguments(arguments)
                        .build());
            }
        }
        return prompts;
    }

    /**
     * 规范化基础 URL
     */
    private String normalizeBaseUrl(String url) {
        String normalized = url.trim();
        if (!normalized.endsWith("/")) {
            normalized += "/";
        }
        return normalized;
    }
}
