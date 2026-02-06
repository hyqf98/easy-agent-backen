package io.github.hijun.agent.entity.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * MCP工具测试请求
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/2/6
 * @since 1.0.0-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "MCP工具测试请求")
public class McpToolTestRequest {

    /**
     * MCP配置ID
     */
    @NotNull(message = "MCP配置ID不能为空")
    @Schema(description = "MCP配置ID", example = "1")
    private Long mcpId;

    /**
     * 工具名称
     */
    @NotBlank(message = "工具名称不能为空")
    @Schema(description = "工具名称", example = "read_file")
    private String toolName;

    /**
     * 工具参数
     */
    @Schema(description = "工具参数（JSON对象）", example = "{\"path\": \"/path/to/file\"}")
    private Map<String, Object> arguments;
}
