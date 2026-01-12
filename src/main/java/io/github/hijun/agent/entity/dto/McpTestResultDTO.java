package io.github.hijun.agent.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MCP 服务器测试结果 DTO
 * <p>
 * 用于返回 MCP 服务器连接测试的结果
 *
 * @author haijun
 * @version 3.4.3
 * @date 2026/01/12
 * @since 3.4.3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpTestResultDTO {

    /**
     * 是否测试成功
     */
    private Boolean success;

    /**
     * 结果消息
     */
    private String message;

    /**
     * 延迟（毫秒）
     */
    private Long latency;

    /**
     * 服务器信息
     */
    private McpServerInfoDTO serverInfo;

    /**
     * 错误详情
     */
    private String error;
}
