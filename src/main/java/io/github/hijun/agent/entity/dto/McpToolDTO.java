package io.github.hijun.agent.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * MCP工具定义
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/2/4 13:17
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "MCP工具定义")
public class McpToolDTO {

    /**
     * name.
     */
    @Schema(description = "工具名称", example = "read_file")
    private String name;

    /**
     * description.
     */
    @Schema(description = "工具描述", example = "读取文件内容")
    private String description;

    /**
     * input schema.
     */
    @Schema(description = "输入Schema")
    private String inputSchema;
}
