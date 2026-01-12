package io.github.hijun.agent.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * MCP 服务器信息 DTO
 * <p>
 * 用于返回 MCP 服务器的详细信息和能力
 *
 * @author haijun
 * @version 3.4.3
 * @date 2026/01/12
 * @since 3.4.3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpServerInfoDTO {

    /**
     * 服务器名称
     */
    private String name;

    /**
     * 服务器版本
     */
    private String version;

    /**
     * 协议版本
     */
    private String protocolVersion;

    /**
     * 服务器能力
     */
    private ServerCapabilities capabilities;

    /**
     * 可用工具列表
     */
    private List<ToolInfo> tools;

    /**
     * 可用资源列表
     */
    private List<ResourceInfo> resources;

    /**
     * 可用提示词列表
     */
    private List<PromptInfo> prompts;

    /**
     * 服务器能力
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServerCapabilities {
        private Boolean tools;
        private Boolean resources;
        private Boolean prompts;
        private Boolean logging;
        private Boolean sampling;
    }

    /**
     * 工具信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolInfo {
        private String name;
        private String description;
        private Object inputSchema;
    }

    /**
     * 资源信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceInfo {
        private String uri;
        private String name;
        private String description;
        private String mimeType;
    }

    /**
     * 提示词信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromptInfo {
        private String name;
        private String description;
        private List<Argument> arguments;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Argument {
            private String name;
            private String description;
            private String required;
        }
    }
}
