package io.github.hijun.agent.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MCP资源定义
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/2/4 13:17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "MCP资源定义")
public class McpResourceDTO {

    /**
     * uri.
     */
    @Schema(description = "资源URI", example = "file:///path/to/file.txt")
    private String uri;

    /**
     * name.
     */
    @Schema(description = "资源名称", example = "file.txt")
    private String name;

    /**
     * description.
     */
    @Schema(description = "资源描述", example = "文本文件")
    private String description;

    /**
     * mime type.
     */
    @Schema(description = "MIME类型", example = "text/plain")
    private String mimeType;
}
