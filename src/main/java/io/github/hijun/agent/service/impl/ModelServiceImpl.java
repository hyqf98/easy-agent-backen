package io.github.hijun.agent.service.impl;

import cn.hutool.core.util.StrUtil;
import io.github.hijun.agent.entity.po.AgentContext;
import io.github.hijun.agent.entity.req.ChatRequest;
import io.github.hijun.agent.service.ModelService;
import io.github.hijun.agent.service.strategy.ReactAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CompletableFuture;

/**
 * React Agent Service Impl - 基于Spring AI的ReAct实现
 *
 * @author haijun
 * @version 3.4.3
 * @date 2025-12-24
 * @email "mailto:haijun@email.com"
 * @since 3.4.3
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelServiceImpl implements ModelService {

    /**
     * Agent
     */
    private final ReactAgent reactAgent;

    /**
     * Chat
     *
     * @param chatRequest chat request
     * @return sse emitter
     * @since 3.4.3
     */
    @Override
    public SseEmitter agent(ChatRequest chatRequest) {
        SseEmitter sseEmitter = new SseEmitter(30_0000L);
        String userPrompt = chatRequest.getUserPrompt();
        if (StrUtil.isNotBlank(userPrompt)) {
            // 添加用户自定义的系统提示词
        }

        AgentContext agentContext = AgentContext.builder()
                .sessionId(chatRequest.getSessionId())
                .sseEmitter(sseEmitter)
                .userQuery(chatRequest.getUserQuery())
                .build();
        agentContext.setSseEmitter(sseEmitter);
        CompletableFuture.runAsync(() -> this.reactAgent.run(agentContext));
        return sseEmitter;
    }
}
