package io.github.hijun.agent.controller;

import io.github.hijun.agent.entity.req.ChatRequest;
import io.github.hijun.agent.service.ReactAgentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Chat Controller
 *
 * @author haijun
 * @email "mailto:haijun@email.com"
 * @date 2025/12/24 16:59
 * @version 3.4.3
 * @since 3.4.3
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    /**
     * react agent service.
     */
    private final ReactAgentService reactAgentService;

    /**
     * 聊天接口（SSE流式返回）
     *
     * @param request 聊天请求
     * @return SSE流
     * @since 3.4.3
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@Valid @RequestBody ChatRequest request) {
        log.info("收到聊天请求，模式：{}，消息：{}", request.getMode(), request.getMessage());
        return this.reactAgentService.executeReact(request);
    }

    /**
     * 健康检查
     *
     * @return OK
     * @since 3.4.3
     */
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
