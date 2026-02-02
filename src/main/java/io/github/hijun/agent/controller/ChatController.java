package io.github.hijun.agent.controller;

import io.github.hijun.agent.entity.req.UserChatRequest;
import io.github.hijun.agent.entity.sse.SseMessage;
import io.github.hijun.agent.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 聊天控制器
 * <p>
 * 提供聊天相关的接口，支持流式响应（SSE）
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Tag(name = "聊天管理", description = "聊天相关接口")
public class ChatController {

    /**
     * ChatService 注入
     */
    private final ChatService chatService;

    /**
     * 流式聊天接口（SSE）
     * <p>
     * 以 Server-Sent Events (SSE) 格式返回大模型的流式响应
     *
     * @param form 聊天请求表单
     * @return Flux<ChatResponse> 流式响应
     * @since 1.0.0-SNAPSHOT
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式聊天", description = "以SSE格式返回大模型的流式响应")
    public Flux<SseMessage<?>> stream(@Valid @RequestBody UserChatRequest form) {
        return this.chatService.streamChat(form);
    }
}
