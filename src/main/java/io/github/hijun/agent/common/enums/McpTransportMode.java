package io.github.hijun.agent.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * MCP 服务器传输模式枚举
 * <p>
 * 定义 MCP (Model Context Protocol) 服务器支持的传输模式
 *
 * @author haijun
 * @version 3.4.3
 * @date 2026/01/12
 * @since 3.4.3
 */
@Getter
@AllArgsConstructor
public enum McpTransportMode {

    /**
     * SSE (Server-Sent Events) 传输模式
     * <p>
     * 使用 Server-Sent Events 进行服务器到客户端的实时推送
     */
    SSE("sse", "SSE (Server-Sent Events)"),

    /**
     * HTTP Stream 传输模式
     * <p>
     * 使用 HTTP 流式传输进行数据交换
     */
    HTTP_STREAM("http_stream", "HTTP Stream");

    /**
     * 传输模式代码
     * <p>
     * 用于唯一标识传输模式，通常是小写字母和下划线
     */
    private final String code;

    /**
     * 传输模式名称
     * <p>
     * 显示给用户的友好名称
     */
    private final String name;

    /**
     * 根据代码获取枚举实例
     * <p>
     * 通过传输模式代码查找对应的枚举常量
     *
     * @param code 传输模式代码
     * @return 对应的枚举实例
     */
    public static McpTransportMode fromCode(String code) {
        for (McpTransportMode mode : values()) {
            if (mode.code.equals(code)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown MCP transport mode: " + code);
    }
}
