package io.github.hijun.agent.entity.req;

import io.github.hijun.agent.common.enums.ConnectionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * MCP配置表单
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/2/2 17:01
 * @since 1.0.0-SNAPSHOT
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "MCP配置表单")
public class McpConfigForm extends BaseForm {

    /**
     * server name.
     */
    @NotBlank(message = "服务器名称不能为空")
    @Size(max = 100, message = "服务器名称长度不能超过100")
    @Schema(description = "服务器名称", example = "filesystem")
    private String serverName;

    /**
     * server desc.
     */
    @Size(max = 200, message = "描述长度不能超过200")
    @Schema(description = "服务器描述", example = "本地文件系统操作")
    private String serverDesc;

    /**
     * connection type.
     */
    @NotNull(message = "连接协议类型不能为空")
    @Schema(description = "连接协议类型", example = "STDIO")
    private ConnectionType connectionType;

    /**
     * command.
     */
    @NotBlank(message = "STDIO命令不能为空")
    @Schema(description = "STDIO命令", example = "npx")
    private String command;

    /**
     * command args.
     */
    @Schema(description = "STDIO命令参数", example = "[\"-y\", \"@modelcontextprotocol/server-filesystem\", \"/Users/data\"]")
    private String commandArgs;

    /**
     * command env.
     */
    @Schema(description = "STDIO环境变量", example = "{\"API_KEY\": \"xxx\"}")
    private String commandEnv;

    /**
     * server url.
     */
    @NotBlank(message = "服务器URL不能为空")
    @Schema(description = "服务器基础URL", example = "http://localhost:3000")
    private String serverUrl;

    /**
     * server endpoint.
     */
    @Schema(description = "服务器端点路径", example = "/mcp/sse")
    private String serverEndpoint;

    /**
     * request timeout.
     */
    @Schema(description = "请求超时时间（秒）", example = "30")
    private Integer requestTimeout;

    /**
     * enabled.
     */
    @NotNull(message = "启用状态不能为空")
    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    /**
     * remark.
     */
    @Size(max = 500, message = "备注长度不能超过500")
    @Schema(description = "备注")
    private String remark;
}
