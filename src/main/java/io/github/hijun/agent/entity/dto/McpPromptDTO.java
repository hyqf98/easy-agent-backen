package io.github.hijun.agent.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * MCP提示词定义
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
@Schema(description = "MCP提示词定义")
public class McpPromptDTO {

    /**
     * name.
     */
    @Schema(description = "提示词名称", example = "summarize")
    private String name;

    /**
     * description.
     */
    @Schema(description = "提示词描述", example = "总结文本内容")
    private String description;

    /**
     * arguments.
     */
    @Schema(description = "参数列表")
    private List<McpPromptArgumentDTO> arguments;
}
