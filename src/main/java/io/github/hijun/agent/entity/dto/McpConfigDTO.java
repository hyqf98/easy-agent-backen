package io.github.hijun.agent.entity.dto;

import io.github.hijun.agent.common.enums.ConnectionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * MCP配置响应实体
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "MCP配置响应")
public class McpConfigDTO extends BaseDTO {

    @Schema(description = "服务器名称", example = "filesystem")
    private String serverName;

    @Schema(description = "服务器描述", example = "本地文件系统操作")
    private String serverDesc;

    @Schema(description = "连接协议类型", example = "STDIO")
    private ConnectionType connectionType;

    @Schema(description = "连接协议类型描述", example = "STDIO协议")
    private String connectionTypeDesc;

    @Schema(description = "STDIO命令")
    private String command;

    @Schema(description = "STDIO命令参数")
    private String commandArgs;

    @Schema(description = "STDIO环境变量")
    private String commandEnv;

    @Schema(description = "服务器基础URL")
    private String serverUrl;

    @Schema(description = "服务器端点路径")
    private String serverEndpoint;

    @Schema(description = "请求超时时间（秒）")
    private Integer requestTimeout;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "备注")
    private String remark;
}
