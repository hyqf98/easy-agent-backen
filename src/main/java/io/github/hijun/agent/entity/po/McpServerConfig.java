package io.github.hijun.agent.entity.po;

import com.baomidou.mybatisplus.annotation.IdGeneratorAssignt;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MCP 服务器配置实体
 * <p>
 * 对应数据库表 mcp_server_config，用于存储 MCP (Model Context Protocol) 服务器的配置信息
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:haijun@email.com"
 * @date 2026/01/11
 * @since 3.4.3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("mcp_server_config")
public class McpServerConfig {

    /**
     * 主键ID
     * <p>
     * 使用雪花算法自动生成
     */
    @TableId(value = "id", type = IdGeneratorAssignt.ASSIGN_ID)
    private String id;

    /**
     * 服务器名称
     * <p>
     * 用于标识和展示 MCP 服务器
     */
    @TableField("server_name")
    private String serverName;

    /**
     * 服务器地址
     * <p>
     * MCP 服务器的连接地址（URL）
     */
    @TableField("server_url")
    private String serverUrl;

    /**
     * 是否启用
     * <p>
     * true 表示启用该服务器，false 表示禁用
     */
    @TableField("enabled")
    private Boolean enabled;

    /**
     * 描述信息
     * <p>
     * 服务器的详细描述说明
     */
    @TableField("description")
    private String description;

    /**
     * 创建时间
     * <p>
     * 记录创建该配置的时间戳（毫秒）
     */
    @TableField("create_time")
    private Long createTime;

    /**
     * 更新时间
     * <p>
     * 记录最后更新该配置的时间戳（毫秒）
     */
    @TableField("update_time")
    private Long updateTime;
}
