package io.github.hijun.agent.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * MCP 连接协议类型
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Getter
@AllArgsConstructor
public enum ConnectionType implements BaseEnum<String> {

    /**
     * STDIO 协议（npx、本地进程）
     */
    STDIO("stdio", "STDIO协议"),

    /**
     * SSE 协议（Server-Sent Events）
     */
    SSE("sse", "SSE协议"),

    /**
     * HTTP Stream 协议
     */
    HTTP_STREAM("http_stream", "HTTP Stream协议");

    /**
     * 连接类型代码
     */
    private final String value;

    /**
     * 连接类型描述
     */
    private final String desc;
}
