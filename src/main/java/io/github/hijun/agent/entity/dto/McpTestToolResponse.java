package io.github.hijun.agent.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MCP工具测试响应
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
@Schema(description = "MCP工具测试响应")
public class McpTestToolResponse {

    /**
     * 工具名称
     */
    @Schema(description = "工具名称", example = "read_file")
    private String toolName;

    /**
     * 执行结果
     */
    @Schema(description = "执行结果")
    private Object result;

    /**
     * 是否成功
     */
    @Schema(description = "是否成功", example = "true")
    private Boolean success;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String error;

    /**
     * 成功响应
     *
     * @param toolName 工具名称
     * @param result   执行结果
     * @return 响应对象
     */
    public static McpTestToolResponse success(String toolName, Object result) {
        return McpTestToolResponse.builder()
                .toolName(toolName)
                .result(result)
                .success(true)
                .build();
    }

    /**
     * 失败响应
     *
     * @param toolName 工具名称
     * @param error    错误信息
     * @return 响应对象
     */
    public static McpTestToolResponse failure(String toolName, String error) {
        return McpTestToolResponse.builder()
                .toolName(toolName)
                .success(false)
                .error(error)
                .build();
    }
}
