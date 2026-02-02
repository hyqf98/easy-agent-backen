package io.github.hijun.agent.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MCP工具定义
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "MCP工具定义")
public class McpToolDTO {

    @Schema(description = "工具名称", example = "read_file")
    private String name;

    @Schema(description = "工具描述", example = "读取文件内容")
    private String description;

    @Schema(description = "输入Schema")
    private String inputSchema;
}
