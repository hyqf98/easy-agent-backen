package io.github.hijun.agent.entity.req;

import io.github.hijun.agent.common.enums.ModelProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 保存配置请求
 * <p>
 * 用于保存系统设置配置，包括 MCP 服务器配置和模型提供商配置
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:haijun@email.com"
 * @date 2026/01/11
 * @since 3.4.3
 */
@Data
public class SaveConfigRequest {

    /**
     * MCP 服务器配置列表
     * <p>
     * 包含所有 MCP 服务器的配置信息
     */
    private List<McpServerConfig> mcpServers;

    /**
     * 模型提供商配置列表
     * <p>
     * 包含所有模型提供商的配置信息
     */
    private List<ModelProviderConfig> modelProviders;

    /**
     * 选中的模型
     * <p>
     * 当前用户选择的模型配置
     */
    private SelectedModel selectedModel;

    /**
     * MCP 服务器配置
     * <p>
     * 用于配置单个 MCP 服务器的信息
     */
    @Data
    public static class McpServerConfig {

        /**
         * 配置ID
         * <p>
         * 更新现有配置时需要提供，新增时为空
         */
        private String id;

        /**
         * 服务器名称
         * <p>
         * 用于标识和展示 MCP 服务器
         */
        @NotBlank(message = "服务器名称不能为空")
        private String name;

        /**
         * 服务器地址
         * <p>
         * MCP 服务器的连接地址（URL）
         */
        @NotBlank(message = "服务器地址不能为空")
        private String url;

        /**
         * 是否启用
         * <p>
         * true 表示启用该服务器，false 表示禁用
         */
        private Boolean enabled;

        /**
         * 描述信息
         * <p>
         * 服务器的详细描述说明
         */
        private String description;
    }

    /**
     * 模型提供商配置
     * <p>
     * 用于配置单个模型提供商的信息
     */
    @Data
    public static class ModelProviderConfig {

        /**
         * 配置ID
         * <p>
         * 更新现有配置时需要提供，新增时为空
         */
        private String id;

        /**
         * 提供商类型
         * <p>
         * 枚举值，表示具体的模型提供商
         */
        @NotNull(message = "提供商类型不能为空")
        private ModelProvider providerType;

        /**
         * 是否启用
         * <p>
         * true 表示启用该提供商，false 表示禁用
         */
        private Boolean enabled;

        /**
         * API Key
         * <p>
         * 用于访问模型提供商 API 的密钥
         */
        private String apiKey;

        /**
         * Base URL
         * <p>
         * 模型提供商的 API 基础地址，可自定义
         */
        private String baseUrl;

        /**
         * 模型列表
         * <p>
         * 该提供商下配置的模型列表
         */
        private List<ModelConfig> models;
    }

    /**
     * 模型配置
     * <p>
     * 用于配置单个模型的信息
     */
    @Data
    public static class ModelConfig {

        /**
         * 模型配置ID
         * <p>
         * 更新现有配置时需要提供，新增时为空
         */
        private String id;

        /**
         * 模型标识
         * <p>
         * 调用 API 时使用的模型标识，如 gpt-4
         */
        @NotBlank(message = "模型标识不能为空")
        private String modelId;

        /**
         * 模型名称
         * <p>
         * 显示给用户的友好名称
         */
        @NotBlank(message = "模型名称不能为空")
        private String modelName;

        /**
         * 是否启用
         * <p>
         * true 表示启用该模型，false 表示禁用
         */
        private Boolean enabled;

        /**
         * 描述信息
         * <p>
         * 该模型的详细描述说明
         */
        private String description;
    }

    /**
     * 选中的模型
     * <p>
     * 表示用户当前选择的模型配置
     */
    @Data
    public static class SelectedModel {

        /**
         * 提供商配置ID
         * <p>
         * 关联 model_provider_config 表的主键
         */
        @NotBlank(message = "提供商ID不能为空")
        private String providerId;

        /**
         * 模型ID
         * <p>
         * 选中的模型标识
         */
        @NotBlank(message = "模型ID不能为空")
        private String modelId;
    }
}
