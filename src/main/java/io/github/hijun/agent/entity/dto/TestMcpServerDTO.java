package io.github.hijun.agent.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 测试 MCP 服务器请求 DTO
 * <p>
 * 用于测试 MCP 服务器连接和获取服务器信息
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
public class TestMcpServerDTO {

    /**
     * 服务器ID
     */
    private String id;

    /**
     * 服务器地址
     */
    @NotBlank(message = "服务器地址不能为空")
    private String url;

    /**
     * 传输模式
     */
    @NotBlank(message = "传输模式不能为空")
    private String transportMode;
}
