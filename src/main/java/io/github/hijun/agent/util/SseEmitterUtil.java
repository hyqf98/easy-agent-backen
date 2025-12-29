package io.github.hijun.agent.util;

import cn.hutool.json.JSONUtil;
import io.github.hijun.agent.common.enums.SseMessageType;
import io.github.hijun.agent.entity.dto.SseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * SSE发送工具类
 *
 * @author haijun
 * @date 2025-12-24
 */
@Slf4j
public class SseEmitterUtil {

    /**
     * 发送消息
     *
     * @param emitter SSE发射器
     * @param message SSE消息
     */
    public static void sendMessage(SseEmitter emitter, SseMessage message) {
        try {
            String json = JSONUtil.toJsonStr(message);
            emitter.send(SseEmitter.event()
                    .data(json)
                    .name("message"));
        } catch (IOException e) {
            log.error("发送SSE消息失败", e);
            emitter.completeWithError(e);
        }
    }

    /**
     * 发送错误消息
     *
     * @param emitter SSE发射器
     * @param messageId 消息ID
     * @param errorMsg 错误消息
     */
    public static void sendError(SseEmitter emitter, String messageId, String errorMsg) {
        SseMessage message = SseMessage.builder()
                .type(SseMessageType.ERROR)
                .messageId(messageId)
                .content(errorMsg)
                .timestamp(System.currentTimeMillis())
                .finished(true)
                .build();
        sendMessage(emitter, message);
        emitter.complete();
    }

    /**
     * 发送完成消息
     *
     * @param emitter SSE发射器
     * @param messageId 消息ID
     */
    public static void sendCompleted(SseEmitter emitter, String messageId) {
        SseMessage message = SseMessage.builder()
                .type(SseMessageType.COMPLETED)
                .messageId(messageId)
                .content("completed")
                .timestamp(System.currentTimeMillis())
                .finished(true)
                .build();
        sendMessage(emitter, message);
        emitter.complete();
    }
}
