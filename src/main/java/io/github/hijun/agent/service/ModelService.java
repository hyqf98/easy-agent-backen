package io.github.hijun.agent.service;

import io.github.hijun.agent.entity.req.ChatRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * React Agent Service
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2025/12/24 16:59
 * @since 3.4.3
 */
public interface ModelService {

    /**
     * Chat
     *
     * @param chatRequest chat request
     * @return sse emitter
     * @since 3.4.3
     */
    SseEmitter agent(ChatRequest chatRequest);
}
