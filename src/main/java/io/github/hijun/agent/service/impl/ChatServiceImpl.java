package io.github.hijun.agent.service.impl;

import io.github.hijun.agent.common.enums.ModelProvider;
import io.github.hijun.agent.common.enums.SseMessageType;
import io.github.hijun.agent.entity.AgentContext;
import io.github.hijun.agent.entity.req.UserChatRequest;
import io.github.hijun.agent.entity.sse.SseMessage;
import io.github.hijun.agent.service.ChatService;
import io.github.hijun.agent.support.DynamicChatClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 聊天服务实现类
 * <p>
 * 提供聊天相关的业务逻辑处理实现，支持动态模型切换
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    /**
     * 动态 ChatClient 工具
     */
    private final DynamicChatClient dynamicChatClient;

    /**
     * 流式聊天
     * <p>
     * 以流式方式调用大模型并返回响应，支持动态选择模型提供商和模型。
     * 使用 AgentContext 封装发送消息的能力
     *
     * @param form 聊天请求表单
     * @return Flux<ChatResponse> 流式响应
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public Flux<SseMessage<?>> streamChat(UserChatRequest form) {
        // 创建 Sinks.Many 用于主动发送数据
        Sinks.Many<SseMessage<?>> sink = Sinks.many().multicast().onBackpressureBuffer();

        // 生成会话 ID
        String sessionId = this.generateSessionId(form);
        String requestId = this.generateRequestId(form);

        // 创建 AgentContext
        AgentContext agentContext = new AgentContext(sink, sessionId, requestId);
        agentContext.setUserMessage(form.getMessage());

        // 异步处理 LLM 调用，避免阻塞订阅
        CompletableFuture.runAsync(() -> {
            try {
                // 确定使用的提供商
                ModelProvider modelProvider = form.getProvider();
                ChatClient chatClient = this.dynamicChatClient.createFromProvider(modelProvider);
                Prompt prompt = new Prompt(new UserMessage(form.getMessage()));

                chatClient.prompt(prompt)
                        .stream()
                        .chatResponse()
                        .doOnNext(response -> {
                            String content = response.getResult().getOutput().getText();
                            if (content != null && !content.isEmpty()) {
                                agentContext.sendMessage(SseMessageType.CONTENT_CHUNK, content);
                            }
                        })
                        .doOnComplete(sink::tryEmitComplete)
                        .doOnError(error -> {
                            agentContext.error(error);
                            sink.tryEmitError(error);
                        })
                        .subscribe();

            } catch (Exception e) {
                log.error("创建流式聊天发生错误: {}", e.getMessage(), e);
                agentContext.error(e);
                sink.tryEmitError(e);
            }
        });

        // 返回可以主动发送数据的 Flux
        return sink.asFlux();
    }

    /**
     * 生成会话 ID
     * <p>
     * 优先使用表单中的 sessionId，否则生成新的 UUID
     *
     * @param form 聊天请求表单
     * @return 会话 ID
     * @since 1.0.0-SNAPSHOT
     */
    private String generateSessionId(UserChatRequest form) {
        if (form.getSessionId() != null && !form.getSessionId().isEmpty()) {
            return form.getSessionId();
        }
        return "sessionId-" + UUID.randomUUID();
    }

    /**
     * Generate Request Id
     *
     * @param form form
     * @return string
     * @since 1.0.0-SNAPSHOT
     */
    private String generateRequestId(UserChatRequest form) {
        if (form.getSessionId() != null && !form.getSessionId().isEmpty()) {
            return form.getSessionId();
        }
        return "requestId-" + UUID.randomUUID();
    }
}
