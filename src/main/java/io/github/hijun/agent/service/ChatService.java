package io.github.hijun.agent.service;

import io.github.hijun.agent.entity.req.UserChatRequest;
import io.github.hijun.agent.entity.sse.SseMessage;
import reactor.core.publisher.Flux;

/**
 * 聊天服务接口
 * <p>
 * 提供聊天相关的业务逻辑处理
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
public interface ChatService {

    /**
     * 流式聊天
     * <p>
     * 以流式方式调用大模型并返回响应
     *
     * @param form 聊天请求表单
     * @return Flux<ChatResponse> 流式响应
     * @since 1.0.0-SNAPSHOT
     */
    Flux<SseMessage<?>> streamChat(UserChatRequest form);
}
