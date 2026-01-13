package io.github.hijun.agent.service.impl;

import cn.hutool.core.collection.CollUtil;
import io.github.hijun.agent.common.enums.AdditionalFeatures;
import io.github.hijun.agent.entity.dto.ContentMessage;
import io.github.hijun.agent.entity.po.AgentContext;
import io.github.hijun.agent.entity.req.ChatRequest;
import io.github.hijun.agent.service.ModelService;
import io.github.hijun.agent.service.strategy.MultiCollaborationAgent;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * React Agent Service Impl - 基于Spring AI的ReAct实现
 *
 * @author haijun
 * @version 3.4.3
 * @date 2025-12-24
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @since 3.4.3
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelServiceImpl implements ModelService, ApplicationContextAware {

    /**
     * Agent
     */
    private final MultiCollaborationAgent multiCollaborationAgent;

    /**
     * application context.
     */
    private ApplicationContext applicationContext;

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

        // 设置当前请求的模型配置（如果提供了）
        String modelProvider = chatRequest.getModelProvider();
        String modelId = chatRequest.getModelId();

        try {
            String userPrompt = chatRequest.getUserPrompt();
            ToolCallback[] toolCallbacks = this.getToolCallbacks(chatRequest);
            AgentContext agentContext = AgentContext.builder()
                    .sessionId(chatRequest.getSessionId())
                    .requestId(chatRequest.getRequestId())
                    .sseEmitter(sseEmitter)
                    .userPrompt(userPrompt)
                    .chatMode(chatRequest.getMode())
                    .userQuery(chatRequest.getUserQuery())
                    .sseEmitter(sseEmitter)
                    .toolCallbacks(Arrays.asList(toolCallbacks))
                    .build();
            CompletableFuture.runAsync(() -> {
                agentContext.sendMessage(ContentMessage.builder().content("PING").build());
                this.multiCollaborationAgent.run(agentContext);
            });
            sseEmitter.onCompletion(() -> {
                log.info("SessionId: {}, RequestId: {} completed", agentContext.getSessionId(), agentContext.getRequestId());
            });
            sseEmitter.onTimeout(() -> {
                log.info("SessionId: {}, RequestId: {} timeout", agentContext.getSessionId(), agentContext.getRequestId());
            });
            return sseEmitter;
        } catch (Exception e) {
            throw e;
        }
    }


    /**
     * Get Tool Callbacks
     *
     * @param chatRequest chat request
     * @return list
     * @since 1.0.0-SNAPSHOT
     */
    private ToolCallback[] getToolCallbacks(ChatRequest chatRequest) {
        // 处理额外的功能
        List<AdditionalFeatures> additionalFeatures = chatRequest.getAdditionalFeatures();

        if (this.applicationContext != null) {
            Map<String, Object> beans = this.applicationContext.getBeansWithAnnotation(Tool.class);
            if (CollUtil.isNotEmpty(beans)) {
                MethodToolCallbackProvider methodToolCallbackProvider = MethodToolCallbackProvider.builder()
                        .toolObjects(beans.values())
                        .build();
                return methodToolCallbackProvider.getToolCallbacks();
            }
        }
        return new ToolCallback[0];
    }

    /**
     * Set Application Context
     *
     * @param applicationContext application context
     * @throws BeansException
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
