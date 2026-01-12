package io.github.hijun.agent.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.hijun.agent.common.enums.McpTransportMode;
import io.github.hijun.agent.common.enums.ModelProvider;
import io.github.hijun.agent.entity.dto.ModelInfoDTO;
import io.github.hijun.agent.entity.dto.ModelProviderDTO;
import io.github.hijun.agent.entity.dto.ProviderInfoDTO;
import io.github.hijun.agent.entity.dto.SettingsConfigDTO;
import io.github.hijun.agent.entity.dto.TestModelResultDTO;
import io.github.hijun.agent.entity.po.McpServerConfig;
import io.github.hijun.agent.entity.po.ModelConfig;
import io.github.hijun.agent.entity.po.ModelProviderConfig;
import io.github.hijun.agent.entity.req.SaveConfigRequest;
import io.github.hijun.agent.entity.req.TestModelRequest;
import io.github.hijun.agent.mapper.McpServerConfigMapper;
import io.github.hijun.agent.mapper.ModelConfigMapper;
import io.github.hijun.agent.mapper.ModelProviderConfigMapper;
import io.github.hijun.agent.service.ModelProviderStrategy;
import io.github.hijun.agent.service.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 设置服务实现
 * <p>
 * 提供系统设置相关的业务逻辑处理，包括 MCP 服务器配置和模型提供商配置的管理
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/01/11
 * @since 3.4.3
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {

    /**
     * MCP 服务器配置 Mapper
     */
    private final McpServerConfigMapper mcpServerConfigMapper;

    /**
     * 模型提供商配置 Mapper
     */
    private final ModelProviderConfigMapper modelProviderConfigMapper;

    /**
     * 模型配置 Mapper
     */
    private final ModelConfigMapper modelConfigMapper;

    /**
     * 模型提供商策略
     */
    private final ModelProviderStrategy modelProviderStrategy;

    /**
     * Get Config
     *
     * @return settings config d t o
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public SettingsConfigDTO getConfig() {
        try {
            // 查询 MCP 服务器配置
            List<McpServerConfig> mcpServerConfigs = this.mcpServerConfigMapper.selectList(null);
            List<SettingsConfigDTO.McpServerDTO> mcpServers = mcpServerConfigs.stream()
                    .map(config -> SettingsConfigDTO.McpServerDTO.builder()
                            .id(config.getId())
                            .name(config.getServerName())
                            .url(config.getServerUrl())
                            .transportMode(config.getTransportMode() != null ? config.getTransportMode().getCode() : "sse")
                            .enabled(config.getEnabled())
                            .description(config.getDescription())
                            .build())
                    .collect(Collectors.toList());

            // 查询模型提供商配置
            List<ModelProviderConfig> providerConfigs = this.modelProviderConfigMapper.selectList(null);
            List<ModelProviderDTO> modelProviders = providerConfigs.stream()
                    .map(config -> {
                        // 查询该提供商下的模型列表
                        List<ModelConfig> modelConfigs;
                        try {
                            modelConfigs = this.modelConfigMapper.selectList(
                                    new LambdaQueryWrapper<ModelConfig>()
                                            .eq(ModelConfig::getProviderConfigId, config.getId())
                            );
                        } catch (Exception e) {
                            log.warn("Failed to load models for provider {}: {}", config.getId(), e.getMessage());
                            modelConfigs = new ArrayList<>();
                        }

                        List<ModelInfoDTO> models = modelConfigs.stream()
                                .map(model -> ModelInfoDTO.builder()
                                        .id(model.getId())
                                        .name(model.getModelName())
                                        .enabled(model.getEnabled())
                                        .build())
                                .collect(Collectors.toList());

                        return ModelProviderDTO.builder()
                                .id(config.getId())
                                .providerType(config.getProviderType())
                                .providerName(config.getProviderType().getName())
                                .enabled(config.getEnabled())
                                .apiKey(this.maskApiKey(config.getApiKey()))
                                .baseUrl(config.getBaseUrl())
                                .models(models)
                                .build();
                    })
                    .collect(Collectors.toList());

            // 查询选中的模型
            SettingsConfigDTO.SelectedModelDTO selectedModel = SettingsConfigDTO.SelectedModelDTO.builder()
                    .providerId("")
                    .modelId("")
                    .build();

            return SettingsConfigDTO.builder()
                    .mcpServers(mcpServers)
                    .modelProviders(modelProviders)
                    .selectedModel(selectedModel)
                    .build();
        } catch (Exception e) {
            log.error("Failed to load config, returning empty data: {}", e.getMessage(), e);
            // 返回空数据而不是抛出异常
            return SettingsConfigDTO.builder()
                    .mcpServers(new ArrayList<>())
                    .modelProviders(new ArrayList<>())
                    .selectedModel(SettingsConfigDTO.SelectedModelDTO.builder()
                            .providerId("")
                            .modelId("")
                            .build())
                    .build();
        }
    }

    /**
     * Save Config
     *
     * @param request request
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveConfig(SaveConfigRequest request) {
        try {
            // 保存 MCP 服务器配置
            if (request.getMcpServers() != null) {
                this.saveMcpServers(request.getMcpServers());
            }

            // 保存模型提供商配置
            if (request.getModelProviders() != null) {
                this.saveModelProviders(request.getModelProviders());
            }
        } catch (Exception e) {
            log.error("Failed to save config: {}", e.getMessage(), e);
            throw new RuntimeException("保存配置失败: " + e.getMessage(), e);
        }
    }

    /**
     * Get Providers
     *
     * @return list
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public List<ProviderInfoDTO> getProviders() {
        try {
            return Stream.of(ModelProvider.values())
                    .map(provider -> ProviderInfoDTO.builder()
                            .providerType(provider)
                            .name(provider.getName())
                            .requireApiKey(provider.getRequireApiKey())
                            .requireBaseUrl(provider.getRequireBaseUrl())
                            .defaultModels(List.of(provider.getDefaultModels()))
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get providers: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Test Model
     *
     * @param request request
     * @return test model result d t o
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public TestModelResultDTO testModel(TestModelRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            // 使用策略模式测试模型连接
            this.modelProviderStrategy.testConnection(request);

            long latency = System.currentTimeMillis() - startTime;
            return TestModelResultDTO.builder()
                    .success(true)
                    .message("连接成功")
                    .latency(latency)
                    .model(request.getModelId())
                    .build();
        } catch (Exception e) {
            log.error("测试模型连接失败: provider={}, model={}",
                    request.getProviderType(), request.getModelId(), e);
            return TestModelResultDTO.builder()
                    .success(false)
                    .message("连接失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 保存 MCP 服务器配置
     * <p>
     * 删除所有现有配置，然后插入新的配置列表
     *
     * @param servers MCP 服务器配置列表
     * @since 1.0.0-SNAPSHOT
     */
    private void saveMcpServers(List<SaveConfigRequest.McpServerConfig> servers) {
        try {
            // 删除所有现有配置
            this.mcpServerConfigMapper.delete(null);

            // 插入新配置
            for (SaveConfigRequest.McpServerConfig server : servers) {
                // 解析传输模式，默认为 SSE
                McpTransportMode transportMode = McpTransportMode.SSE;
                if (server.getTransportMode() != null && !server.getTransportMode().isEmpty()) {
                    try {
                        transportMode = McpTransportMode.fromCode(server.getTransportMode());
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid transport mode '{}', using default SSE", server.getTransportMode());
                    }
                }

                McpServerConfig config = McpServerConfig.builder()
                        .serverName(server.getName())
                        .serverUrl(server.getUrl())
                        .transportMode(transportMode)
                        .enabled(server.getEnabled() != null ? server.getEnabled() : true)
                        .description(server.getDescription())
                        .createTime(System.currentTimeMillis())
                        .updateTime(System.currentTimeMillis())
                        .build();
                this.mcpServerConfigMapper.insert(config);
            }
        } catch (Exception e) {
            log.error("Failed to save MCP servers: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 保存模型提供商配置
     * <p>
     * 根据配置 ID 判断是新增还是更新，并保存关联的模型列表
     *
     * @param providers 模型提供商配置列表
     * @since 1.0.0-SNAPSHOT
     */
    private void saveModelProviders(List<SaveConfigRequest.ModelProviderConfig> providers) {
        for (SaveConfigRequest.ModelProviderConfig provider : providers) {
            try {
                if (StrUtil.isNotBlank(provider.getId())) {
                    // 更新现有配置
                    ModelProviderConfig config = ModelProviderConfig.builder()
                            .id(provider.getId())
                            .providerType(provider.getProviderType())
                            .enabled(provider.getEnabled() != null ? provider.getEnabled() : true)
                            .apiKey(provider.getApiKey())
                            .baseUrl(provider.getBaseUrl())
                            .updateTime(System.currentTimeMillis())
                            .build();
                    this.modelProviderConfigMapper.updateById(config);

                    // 删除并重新添加模型
                    try {
                        this.modelConfigMapper.delete(
                                new LambdaQueryWrapper<ModelConfig>()
                                        .eq(ModelConfig::getProviderConfigId, provider.getId())
                        );
                    } catch (Exception e) {
                        log.warn("Failed to delete models for provider {}: {}", provider.getId(), e.getMessage());
                    }
                    this.saveModels(provider.getId(), provider.getModels());
                } else {
                    // 插入新配置
                    ModelProviderConfig config = ModelProviderConfig.builder()
                            .providerType(provider.getProviderType())
                            .enabled(provider.getEnabled() != null ? provider.getEnabled() : true)
                            .apiKey(provider.getApiKey())
                            .baseUrl(provider.getBaseUrl())
                            .createTime(System.currentTimeMillis())
                            .updateTime(System.currentTimeMillis())
                            .build();
                    this.modelProviderConfigMapper.insert(config);

                    // 添加模型
                    if (provider.getModels() != null) {
                        this.saveModels(config.getId(), provider.getModels());
                    }
                }
            } catch (Exception e) {
                log.error("Failed to save model provider {}: {}", provider.getProviderType(), e.getMessage());
                throw e;
            }
        }
    }

    /**
     * 保存模型配置
     * <p>
     * 批量插入模型配置到数据库
     *
     * @param providerConfigId 提供商配置 ID
     * @param models           模型配置列表
     * @since 1.0.0-SNAPSHOT
     */
    private void saveModels(String providerConfigId, List<SaveConfigRequest.ModelConfig> models) {
        if (models == null) {
            return;
        }

        for (SaveConfigRequest.ModelConfig model : models) {
            ModelConfig config = ModelConfig.builder()
                    .providerConfigId(providerConfigId)
                    .modelId(model.getModelId())
                    .modelName(model.getModelName())
                    .enabled(model.getEnabled() != null ? model.getEnabled() : true)
                    .description(model.getDescription())
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis())
                    .build();
            this.modelConfigMapper.insert(config);
        }
    }

    /**
     * 脱敏 API Key
     * <p>
     * 对 API Key 进行脱敏处理，只显示前 7 位和后 4 位字符
     *
     * @param apiKey 原始 API Key
     * @return 脱敏后的 API Key
     * @since 1.0.0-SNAPSHOT
     */
    private String maskApiKey(String apiKey) {
        if (StrUtil.isBlank(apiKey)) {
            return "";
        }
        if (apiKey.length() <= 8) {
            return "sk-****";
        }
        return apiKey.substring(0, 7) + "..." + apiKey.substring(apiKey.length() - 4);
    }
}
