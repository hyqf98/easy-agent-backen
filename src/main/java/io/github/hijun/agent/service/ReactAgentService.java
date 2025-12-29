package io.github.hijun.agent.service;

import io.github.hijun.agent.entity.req.ChatRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * React Agent Service
 *
 * @author haijun
 * @email "mailto:haijun@email.com"
 * @date 2025/12/24 16:59
 * @version 3.4.3
 * @since 3.4.3
 */
public interface ReactAgentService {
    /**
     * 执行ReAct流程
     *
     * @param request 请求
     * @return SSE发射器
     * @since 3.4.3
     */
    SseEmitter executeReact(ChatRequest request);
}
