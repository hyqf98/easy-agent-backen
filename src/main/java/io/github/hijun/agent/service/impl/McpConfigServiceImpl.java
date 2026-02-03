package io.github.hijun.agent.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hijun.agent.common.exception.BusinessException;
import io.github.hijun.agent.converter.McpConfigConverter;
import io.github.hijun.agent.entity.dto.CommandEnv;
import io.github.hijun.agent.entity.dto.McpConfigDTO;
import io.github.hijun.agent.entity.dto.McpPromptArgumentDTO;
import io.github.hijun.agent.entity.dto.McpPromptDTO;
import io.github.hijun.agent.entity.dto.McpResourceDTO;
import io.github.hijun.agent.entity.dto.McpToolDTO;
import io.github.hijun.agent.entity.po.McpConfig;
import io.github.hijun.agent.entity.req.McpConfigForm;
import io.github.hijun.agent.entity.req.McpConfigQuery;
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
                McpSchema.InitializeResult result = client.initialize();
                log.info("MCP服务器连接测试成功: {}, 服务器: {}", config.getServerName(), result.serverInfo());
            }
        } catch (Exception e) {
            log.error("MCP服务器连接测试失败: {}", config.getServerName(), e);
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
            log.error("获取MCP工具列表失败: {}", config.getServerName(), e);
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
            log.error("获取MCP资源列表失败: {}", config.getServerName(), e);
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
            log.error("获取MCP提示列表失败: {}", config.getServerName(), e);
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
            log.error("创建MCP客户端失败: {}", config.getServerName(), e);
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
        List<CommandEnv> envList = this.parseCommandEnv(config.getCommandEnv());
        Map<String, String> env = this.toEnvMap(envList);
        if (ObjectUtil.isNotEmpty(env)) {
            builder.env(env);
        }

        ServerParameters params = builder.build();

        // 创建 STDIO 传输层，需要传入 McpJsonMapper
        StdioClientTransport transport = new StdioClientTransport(params, new JacksonMcpJsonMapper(new ObjectMapper()));

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
     * 解析环境变量 JSON 字符串为 CommandEnv 列表
     *
     * @param commandEnv 环境变量 JSON 字符串
     * @return CommandEnv 列表
     * @since 1.0.0-SNAPSHOT
     */
    private List<CommandEnv> parseCommandEnv(String commandEnv) {
        if (StrUtil.isBlank(commandEnv)) {
            return List.of();
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
     * Convert CommandEnv List to Map
     * <p>
     * 将 CommandEnv 列表转换为 Map
     *
     * @param envList CommandEnv 列表
     * @return 环境变量 Map
     * @since 1.0.0-SNAPSHOT
     */
    private Map<String, String> toEnvMap(List<CommandEnv> envList) {
        if (ObjectUtil.isEmpty(envList)) {
            return Map.of();
        }
        return envList.stream()
                .collect(java.util.stream.Collectors.toMap(
                        CommandEnv::key,
                        CommandEnv::value,
                        (v1, v2) -> v1));
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
}
