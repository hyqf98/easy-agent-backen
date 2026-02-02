package io.github.hijun.agent.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MCP提示词参数定义
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "MCP提示词参数定义")
public class McpPromptArgumentDTO {

    @Schema(description = "参数名称", example = "text")
    private String name;

    @Schema(description = "参数描述", example = "要总结的文本")
    private String description;

    @Schema(description = "是否必填", example = "true")
    private Boolean required;
}
