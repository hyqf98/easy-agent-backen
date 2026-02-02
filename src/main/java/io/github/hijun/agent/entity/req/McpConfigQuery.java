package io.github.hijun.agent.entity.req;

import io.github.hijun.agent.common.enums.ConnectionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * MCP配置查询实体
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/2/2 17:28
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "MCP配置查询实体")
public class McpConfigQuery extends BaseQuery {

    /**
     * connection type.
     */
    @Schema(description = "连接协议类型")
    private ConnectionType connectionType;

    /**
     * enabled.
     */
    @Schema(description = "是否启用")
    private Boolean enabled;

    /**
     * keyword.
     */
    @Schema(description = "关键词搜索")
    private String keyword;
}
