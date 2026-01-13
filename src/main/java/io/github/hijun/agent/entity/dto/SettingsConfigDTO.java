package io.github.hijun.agent.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 系统配置 DTO
 * <p>
 * 用于返回系统设置配置信息，包括 MCP 服务器配置和模型提供商配置
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/01/11
 * @since 3.4.3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsConfigDTO {

    /**
     * MCP 服务器配置列表
     * <p>
     * 包含所有 MCP 服务器的配置信息
     */
    private List<McpServerDTO> mcpServers;

    /**
     * 模型提供商配置列表
     * <p>
     * 包含所有模型提供商的配置信息
     */
    private List<ModelProviderDTO> modelProviders;

    /**
     * 选中的模型
     * <p>
     * 当前用户选择的模型配置
     */
    private SelectedModelDTO selectedModel;

    /**
     * MCP 服务器配置 DTO
     * <p>
     * 表示单个 MCP 服务器的配置信息
     *
     * @author haijun
     * @email "mailto:iamxiaohaijun@gmail.com"
     * @date 2026/1/13 17:15
     * @version 1.0.0-SNAPSHOT
     * @since 1.0.0-SNAPSHOT
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    @NoArgsConstructor
    public static class McpServerDTO extends BaseDTO {

        /**
         * 服务器名称
         * <p>
         * 用于标识和展示 MCP 服务器
         */
        private String name;

        /**
         * 服务器地址
         * <p>
         * MCP 服务器的连接地址（URL）
         */
        private String url;

        /**
         * 传输模式
         * <p>
         * MCP 服务器的传输模式（sse 或 http_stream）
         */
        private String transportMode;

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
     * 选中的模型 DTO
     * <p>
     * 表示用户当前选择的模型配置
     *
     * @author haijun
     * @email "mailto:iamxiaohaijun@gmail.com"
     * @date 2026/1/13 17:15
     * @version 1.0.0-SNAPSHOT
     * @since 1.0.0-SNAPSHOT
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SelectedModelDTO extends BaseDTO {

        /**
         * 提供商配置ID
         * <p>
         * 关联 model_provider_config 表的主键
         */
        private String providerId;

        /**
         * 模型ID
         * <p>
         * 选中的模型标识
         */
        private String modelId;
    }
}
