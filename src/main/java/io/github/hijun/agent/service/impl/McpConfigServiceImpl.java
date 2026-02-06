package io.github.hijun.agent.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import io.github.hijun.agent.common.enums.ConnectionType;
import io.github.hijun.agent.common.exception.BusinessException;
import io.github.hijun.agent.converter.McpConfigConverter;
import io.github.hijun.agent.entity.dto.McpConfigDTO;
import io.github.hijun.agent.entity.dto.McpPromptArgumentDTO;
import io.github.hijun.agent.entity.dto.McpPromptDTO;
import io.github.hijun.agent.entity.dto.McpResourceDTO;
import io.github.hijun.agent.entity.dto.McpTestToolResponse;
import io.github.hijun.agent.entity.dto.McpToolDTO;
import io.github.hijun.agent.entity.po.McpConfig;
import io.github.hijun.agent.entity.req.McpConfigForm;
import io.github.hijun.agent.entity.req.McpConfigQuery;
import io.github.hijun.agent.entity.req.McpToolTestRequest;
import io.github.hijun.agent.mapper.McpConfigMapper;
import io.github.hijun.agent.service.McpConfigService;
import io.github.hijun.agent.utils.Jsons;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * MCP配置服务实现
 * <p>
 * 提供 MCP 配置的增删改查服务实现，支持 STDIO、SSE、HTTP_STREAM 三种连接协议
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/2/2 16:59
 * @since 1.0.0-SNAPSHOT
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class McpConfigServiceImpl extends ServiceImpl<McpConfigMapper, McpConfig> implements McpConfigService {

    /**
     * Page
     *
     * @param query query
     * @return page
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public IPage<McpConfigDTO> page(McpConfigQuery query) {
        IPage<McpConfig> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<McpConfig> wrapper = this.buildQueryWrapper(query);
        IPage<McpConfig> result = super.page(page, wrapper);
        return McpConfigConverter.INSTANCE.toDto(result);
    }

    /**
     * Get By Id
     *
     * @param id id
     * @return mcp config d t o
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public McpConfigDTO getById(Long id) {
        McpConfig entity = super.getById(id);
        return Optional.ofNullable(entity).map(McpConfigConverter.INSTANCE::toDto).orElse(null);
    }

    /**
     * Create
     *
     * @param form form
     * @return boolean
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public boolean create(McpConfigForm form) {
        Assert.notNull(form, "表单数据不能为空");
        this.validateFormByConnectionType(form);

        // 检查 serverName 是否重复
        LambdaQueryWrapper<McpConfig> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(McpConfig::getServerName, form.getServerName());
        if (this.count(checkWrapper) > 0) {
            throw new BusinessException("服务器名称已存在");
        }

        McpConfig entity = McpConfigConverter.INSTANCE.toPo(form);
        return this.save(entity);
    }

    /**
     * Update
     *
     * @param form form
     * @return boolean
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public boolean update(McpConfigForm form) {
        Assert.notNull(form, "表单数据不能为空");
        this.validateFormByConnectionType(form);

        // 检查 serverName 是否重复
        LambdaQueryWrapper<McpConfig> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(McpConfig::getServerName, form.getServerName());
        checkWrapper.ne(McpConfig::getId, form.getId());
        if (this.count(checkWrapper) > 0) {
            throw new BusinessException("服务器名称已存在");
        }

        McpConfig entity = McpConfigConverter.INSTANCE.toPo(form);
        return this.updateById(entity);
    }

    /**
     * Validate Form By Connection Type
     * <p>
     * 根据连接协议类型验证表单字段
     *
     * @param form 表单数据
     * @since 1.0.0-SNAPSHOT
     */
    private void validateFormByConnectionType(McpConfigForm form) {
        Assert.notNull(form.getConnectionType(), "连接协议类型不能为空");

        switch (form.getConnectionType()) {
            case STDIO -> {
                Assert.hasText(form.getCommand(), "STDIO类型的命令不能为空");
            }
            case SSE -> {
                Assert.hasText(form.getServerUrl(), "SSE类型的服务器URL不能为空");
            }
            case HTTP_STREAM -> {
                Assert.hasText(form.getServerUrl(), "HTTP_STREAM类型的服务器URL不能为空");
            }
        }
    }

    /**
     * Remove By Ids
     *
     * @param ids ids
     * @return boolean
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public boolean removeByIds(List<Long> ids) {
        Assert.notEmpty(ids, "删除ID列表不能为空");
        return super.removeByIds(ids);
    }

    /**
     * Get Tool Callbacks By Ids
     *
     * @param ids ids
     * @return tool callback list
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public List<ToolCallback> getToolCallbacksByIds(List<Long> ids) {
        Assert.notEmpty(ids, "MCP配置ID列表不能为空");

        // 查询所有启用的配置
        LambdaQueryWrapper<McpConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(McpConfig::getId, ids);
        wrapper.eq(McpConfig::getEnabled, true);
        List<McpConfig> configs = this.list(wrapper);

        if (configs.isEmpty()) {
            return List.of();
        }

        // 创建所有 MCP 客户端
        List<McpSyncClient> clients = new ArrayList<>();
        for (McpConfig config : configs) {
            try {
                McpSyncClient client = this.createMcpClient(config);
                if (client != null) {
                    clients.add(client);
                }
            } catch (Exception e) {
                log.error("创建MCP客户端失败: {}", config.getServerName(), e);
            }
        }

        // 使用 Spring AI 提供的工具类获取 ToolCallback
        return McpToolUtils.getToolCallbacksFromSyncClients(clients);
    }

    /**
     * Test Connection
     *
     * @param id id
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public void testConnection(Long id) {
        Assert.notNull(id, "MCP配置ID不能为空");

        McpConfig config = super.getById(id);
        if (config == null) {
            throw new BusinessException("MCP配置不存在");
        }

        try (McpSyncClient client = this.createMcpClient(config)) {
            if (client != null) {
                // 初始化连接
                client.initialize();
            }
        } catch (Exception e) {
            throw new BusinessException("连接测试失败: " + e.getMessage());
        }
    }

    /**
     * List Tools
     *
     * @param id id
     * @return tool list
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public List<McpToolDTO> listTools(Long id) {
        Assert.notNull(id, "MCP配置ID不能为空");

        McpConfig config = super.getById(id);
        if (config == null) {
            throw new BusinessException("MCP配置不存在");
        }

        try (McpSyncClient client = this.createMcpClient(config)) {
            if (client != null) {
                client.initialize();
                McpSchema.ListToolsResult result = client.listTools();
                return result.tools().stream()
                        .map(tool -> McpToolDTO.builder()
                                .name(tool.name())
                                .description(tool.description())
                                .inputSchema(Jsons.toJson(tool.inputSchema()))
                                .build())
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            throw new BusinessException("获取工具列表失败: " + e.getMessage());
        }

        return List.of();
    }

    /**
     * List Resources
     *
     * @param id id
     * @return resource list
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public List<McpResourceDTO> listResources(Long id) {
        Assert.notNull(id, "MCP配置ID不能为空");

        McpConfig config = super.getById(id);
        if (config == null) {
            throw new BusinessException("MCP配置不存在");
        }

        try (McpSyncClient client = this.createMcpClient(config)) {
            if (client != null) {
                client.initialize();
                McpSchema.ListResourcesResult result = client.listResources();
                return result.resources().stream()
                        .map(resource -> McpResourceDTO.builder()
                                .uri(resource.uri())
                                .name(resource.name())
                                .description(resource.description())
                                .mimeType(resource.mimeType())
                                .build())
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            throw new BusinessException("获取资源列表失败: " + e.getMessage());
        }
        return List.of();
    }

    /**
     * List Prompts
     *
     * @param id id
     * @return prompt list
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public List<McpPromptDTO> listPrompts(Long id) {
        Assert.notNull(id, "MCP配置ID不能为空");

        McpConfig config = super.getById(id);
        if (config == null) {
            throw new BusinessException("MCP配置不存在");
        }

        try (McpSyncClient client = this.createMcpClient(config)) {
            if (client != null) {
                client.initialize();
                McpSchema.ListPromptsResult result = client.listPrompts();
                return result.prompts().stream()
                        .map(prompt -> McpPromptDTO.builder()
                                .name(prompt.name())
                                .description(prompt.description())
                                .arguments(prompt.arguments().stream()
                                        .map(arg -> McpPromptArgumentDTO.builder()
                                                .name(arg.name())
                                                .description(arg.description())
                                                .required(arg.required())
                                                .build())
                                        .collect(Collectors.toList()))
                                .build())
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            throw new BusinessException("获取提示列表失败: " + e.getMessage());
        }
        return List.of();
    }

    /**
     * Create MCP Client
     * <p>
     * 根据配置创建同步 MCP 客户端，支持 STDIO、SSE、HTTP_STREAM 三种连接类型
     *
     * @param config MCP配置
     * @return McpSyncClient
     * @since 1.0.0-SNAPSHOT
     */
    private McpSyncClient createMcpClient(McpConfig config) {
        if (config == null || config.getConnectionType() == null) {
            throw new IllegalArgumentException("MCP配置或连接类型不能为空");
        }

        try {
            // 使用 switch 根据连接类型创建不同客户端
            return switch (config.getConnectionType()) {
                case STDIO -> this.createStdioClient(config);
                case SSE -> this.createSseClient(config);
                case HTTP_STREAM -> this.createHttpStreamClient(config);
            };
        } catch (Exception e) {
            throw new BusinessException("创建MCP客户端失败: " + e.getMessage());
        }
    }

    /**
     * Create STDIO Client
     * <p>
     * 创建 STDIO（标准输入输出）协议的 MCP 客户端
     *
     * @param config MCP配置
     * @return McpSyncClient
     * @since 1.0.0-SNAPSHOT
     */
    private McpSyncClient createStdioClient(McpConfig config) {
        Assert.hasText(config.getCommand(), "STDIO命令不能为空");

        // 解析命令参数
        List<String> args = this.parseCommandArgs(config.getCommandArgs());

        // 构建服务器参数
        ServerParameters.Builder builder = ServerParameters.builder(config.getCommand());
        if (ObjectUtil.isNotEmpty(args)) {
            builder.args(args.toArray(new String[0]));
        }

        // 解析环境变量
        Map<String, String> env = this.parseCommandEnv(config.getCommandEnv());
        if (ObjectUtil.isNotEmpty(env)) {
            builder.env(env);
        }

        ServerParameters params = builder.build();

        // 创建 STDIO 传输层，需要传入 McpJsonMapper
        StdioClientTransport transport = new StdioClientTransport(params, new JacksonMcpJsonMapper(Jsons.getObjectMapper()));

        // 构建客户端
        return McpClient
                .sync(transport)
                .clientInfo(new McpSchema.Implementation(config.getServerName(), "1.0.0"))
                .requestTimeout(Duration.ofSeconds(
                        config.getRequestTimeout() != null && config.getRequestTimeout() > 0
                                ? config.getRequestTimeout() : 30))
                .build();
    }

    /**
     * Create SSE Client
     * <p>
     * 创建 SSE（Server-Sent Events）协议的 MCP 客户端
     *
     * @param config MCP配置
     * @return McpSyncClient
     * @since 1.0.0-SNAPSHOT
     */
    private McpSyncClient createSseClient(McpConfig config) {
        Assert.hasText(config.getServerUrl(), "服务器URL不能为空");

        // SSE 传输暂不支持，需要使用 HttpClientSseClientTransport
        // 该传输层需要更多参数配置，这里先抛出异常提示
        throw new BusinessException("SSE协议暂未支持，请使用STDIO协议");
    }

    /**
     * Create HTTP Stream Client
     * <p>
     * 创建 HTTP Stream 协议的 MCP 客户端
     *
     * @param config MCP配置
     * @return McpSyncClient
     * @since 1.0.0-SNAPSHOT
     */
    private McpSyncClient createHttpStreamClient(McpConfig config) {
        Assert.hasText(config.getServerUrl(), "服务器URL不能为空");

        // HTTP Stream 传输暂不支持
        throw new BusinessException("HTTP_STREAM协议暂未支持，请使用STDIO协议");
    }

    /**
     * Parse Command Args
     * <p>
     * 解析命令参数 JSON 字符串为字符串列表
     *
     * @param commandArgs 命令参数 JSON 字符串
     * @return 参数列表
     * @since 1.0.0-SNAPSHOT
     */
    private List<String> parseCommandArgs(String commandArgs) {
        if (StrUtil.isBlank(commandArgs)) {
            return List.of();
        }

        try {
            return Jsons.parse(commandArgs, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("解析命令参数失败: {}", commandArgs, e);
            throw new BusinessException("命令参数格式错误，必须是JSON数组格式");
        }
    }

    /**
     * Parse Command Env
     * <p>
     * 解析环境变量 JSON 字符串为 Map
     *
     * @param commandEnv 环境变量 JSON 字符串
     * @return 环境变量 Map
     * @since 1.0.0-SNAPSHOT
     */
    private Map<String, String> parseCommandEnv(String commandEnv) {
        if (StrUtil.isBlank(commandEnv)) {
            return Map.of();
        }

        try {
            return Jsons.parse(commandEnv, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("解析环境变量失败: {}", commandEnv, e);
            throw new BusinessException("环境变量格式错误，必须是JSON对象格式");
        }
    }

    /**
     * Build Query Wrapper
     * <p>
     * 构建查询条件
     *
     * @param query 查询条件
     * @return LambdaQueryWrapper
     * @since 1.0.0-SNAPSHOT
     */
    private LambdaQueryWrapper<McpConfig> buildQueryWrapper(McpConfigQuery query) {
        if (ObjectUtil.isNull(query)) {
            return new LambdaQueryWrapper<>();
        }

        LambdaQueryWrapper<McpConfig> wrapper = new LambdaQueryWrapper<McpConfig>()
                .eq(query.getConnectionType() != null, McpConfig::getConnectionType, query.getConnectionType())
                .eq(query.getEnabled() != null, McpConfig::getEnabled, query.getEnabled())
                .ge(query.getStartTime() != null, McpConfig::getCreateTime, query.getStartTime())
                .lt(query.getEndTime() != null, McpConfig::getCreateTime, query.getEndTime())
                .orderByDesc(McpConfig::getCreateTime);

        // 关键词搜索
        Optional.ofNullable(query.getKeyword())
                .filter(StrUtil::isNotBlank)
                .ifPresent(keyword -> wrapper.and(w -> w
                        .like(McpConfig::getServerName, keyword)
                        .or()
                        .like(McpConfig::getServerDesc, keyword)));

        return wrapper;
    }

    /**
     * Build Friendly Error Message
     * <p>
     * 将原始异常转换为用户友好的错误信息
     *
     * @param config MCP配置
     * @param e      异常
     * @return 友好的错误信息
     * @since 1.0.0-SNAPSHOT
     */
    private String buildFriendlyErrorMessage(McpConfig config, Exception e) {
        // 获取根本原因
        Throwable rootCause = this.getRootCause(e);

        // 处理超时异常
        if (rootCause instanceof TimeoutException) {
            return "连接超时，服务器响应时间过长，请检查网络或增加超时时间";
        }

        // 处理进程终止异常
        String message = rootCause.getMessage();
        if (message != null && message.contains("Process terminated with code")) {
            return "MCP进程异常终止，请检查命令配置是否正确";
        }

        // 处理初始化失败
        if (message != null && message.contains("Client failed to initialize")) {
            return config.getConnectionType() == ConnectionType.STDIO
                    ? "STDIO进程启动失败，请检查命令和环境变量配置"
                    : "服务器连接失败，请检查URL和网络配置";
        }

        // 其他异常返回简要信息
        return rootCause.getMessage() != null
                ? rootCause.getMessage()
                : "未知错误";
    }

    /**
     * Get Root Cause
     * <p>
     * 获取异常的根本原因
     *
     * @param e 异常
     * @return 根本原因
     * @since 1.0.0-SNAPSHOT
     */
    private Throwable getRootCause(Throwable e) {
        Throwable cause = e;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }

    /**
     * Test Tool
     *
     * @param request request
     * @return mcp test tool response
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public McpTestToolResponse testTool(McpToolTestRequest request) {
        Assert.notNull(request, "测试请求不能为空");
        Assert.notNull(request.getMcpId(), "MCP配置ID不能为空");
        Assert.hasText(request.getToolName(), "工具名称不能为空");

        // 查询MCP配置
        McpConfig config = super.getById(request.getMcpId());
        if (config == null) {
            return McpTestToolResponse.failure(request.getToolName(), "MCP配置不存在");
        }

        try (McpSyncClient client = this.createMcpClient(config)) {
            if (client != null) {
                client.initialize();

                // 构建调用参数
                McpSchema.CallToolRequest callToolRequest = new McpSchema.CallToolRequest(
                        request.getToolName(),
                        request.getArguments() != null ? request.getArguments() : Map.of()
                );

                // 调用工具
                McpSchema.CallToolResult result = client.callTool(callToolRequest);

                // 返回结果
                return McpTestToolResponse.success(request.getToolName(), result.content());
            }
        } catch (Exception e) {
            log.error("测试MCP工具失败: {}", request.getToolName(), e);
            return McpTestToolResponse.failure(request.getToolName(), e.getMessage());
        }

        return McpTestToolResponse.failure(request.getToolName(), "创建MCP客户端失败");
    }
}
