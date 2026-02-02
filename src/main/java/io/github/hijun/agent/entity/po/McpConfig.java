package io.github.hijun.agent.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.hijun.agent.common.enums.ConnectionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * MCP服务器配置
 * <p>
 * 存储 MCP (Model Context Protocol) 服务器的连接配置信息，支持 STDIO、SSE、HTTP_STREAM 等连接类型
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("mcp_config")
public class McpConfig extends BasePo implements Serializable {

    /**
     * MCP服务器名称（唯一标识）
     * <p>
     * 如：mcp-server-fs、mcp-server-github
     */
    @TableField("server_name")
    private String serverName;

    /**
     * MCP服务器描述
     * <p>
     * 如：本地文件系统、GitHub 仓库
     */
    @TableField("server_desc")
    private String serverDesc;

    /**
     * 连接协议类型
     * <p>
     * STDIO、SSE、HTTP_STREAM
     */
    @TableField("connection_type")
    private ConnectionType connectionType;

    /**
     * STDIO命令（connection_type=STDIO 时使用）
     * <p>
     * 如：npx、uvx
     */
    @TableField("command")
    private String command;

    /**
     * STDIO命令参数（JSON数组字符串）
     * <p>
     * 如：["-y", "@modelcontextprotocol/server-filesystem", "/path"]
     */
    @TableField("command_args")
    private String commandArgs;

    /**
     * STDIO环境变量（JSON对象字符串）
     * <p>
     * 如：{"API_KEY": "xxx", "DEBUG": "true"}
     */
    @TableField("command_env")
    private String commandEnv;

    /**
     * 服务器基础URL（connection_type=SSE/HTTP_STREAM 时使用）
     * <p>
     * 如：http://localhost:3000、https://api.example.com
     */
    @TableField("server_url")
    private String serverUrl;

    /**
     * 服务器端点路径
     * <p>
     * SSE 协议默认 /sse，HTTP Stream 协议默认 /mcp
     */
    @TableField("server_endpoint")
    private String serverEndpoint;

    /**
     * 请求超时时间
     * <p>
     * 单位：秒
     */
    @TableField("request_timeout")
    private Integer requestTimeout;

    /**
     * 是否启用
     * <p>
     * true-启用，false-禁用
     */
    @TableField("enabled")
    private Boolean enabled;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}
