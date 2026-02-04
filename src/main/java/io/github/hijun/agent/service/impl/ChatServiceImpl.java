package io.github.hijun.agent.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import io.github.hijun.agent.agent.DataCollectorAgent;
import io.github.hijun.agent.agent.ReActLLM;
import io.github.hijun.agent.entity.AgentContext;
import io.github.hijun.agent.entity.dto.LlmModelDTO;
import io.github.hijun.agent.entity.req.UserChatRequest;
import io.github.hijun.agent.entity.sse.SseMessage;
import io.github.hijun.agent.service.ChatService;
import io.github.hijun.agent.service.LlmModelService;
import io.github.hijun.agent.support.DynamicChatClient;
import io.github.hijun.agent.support.FunctionToolFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 聊天服务实现类
 * <p>
 * 提供聊天相关的业务逻辑处理实现，支持通过模型ID动态获取配置并创建 ChatClient
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/2/2 17:50
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    /**
     * 大语言模型配置服务
     */
    private final LlmModelService llmModelService;

    /**
     * 动态 ChatClient 工具
     */
    private final DynamicChatClient dynamicChatClient;

    /**
     * function tool factory.
     */
    private final FunctionToolFactory functionToolFactory;

    /**
     * 异步任务执行器
     */
    private final Executor executor = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r);
        thread.setName("chat-async-" + thread.getId());
        thread.setDaemon(false);
        return thread;
    });

    /**
     * 流式聊天
     * <p>
     * 以流式方式调用大模型并返回响应，根据 modelId 从数据库查询模型配置并动态创建 ChatClient
     *
     * @param userChatRequest 聊天请求表单
     * @return Flux<SseMessage<?>> 流式响应
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public Flux<SseMessage<?>> streamChat(UserChatRequest userChatRequest) {
        log.info("开始处理流式聊天请求: {}", userChatRequest);

        // 创建 Sinks.Many 用于主动发送数据
        Sinks.Many<SseMessage<?>> sink = Sinks.many().multicast().onBackpressureBuffer();

        // 生成会话 ID 和请求 ID
        String sessionId = this.generateSessionId(userChatRequest);
        String requestId = this.generateRequestId(userChatRequest);

        log.info("生成会话ID: {}, 请求ID: {}", sessionId, requestId);

        Long modelId = userChatRequest.getModelId();

        // 根据 modelId 查询模型配置
        LlmModelDTO modelConfig = this.llmModelService.getById(modelId);
        if (modelConfig == null) {
            log.error("模型配置不存在: modelId={}", modelId);
            throw new IllegalArgumentException("模型配置不存在，modelId: " + modelId);
        }

        log.info("查询到模型配置: modelCode={}, modelName={}", modelConfig.getModelCode(), modelConfig.getModelName());

        // 检查模型是否启用
        if (!modelConfig.getEnabled()) {
            log.error("模型已禁用: modelId={}", modelId);
            throw new IllegalArgumentException("模型已禁用，modelId: " + modelId);
        }

        // 创建 AgentContext
        AgentContext agentContext = new AgentContext(sink, modelConfig, sessionId, requestId);
        String userMessage = userChatRequest.getMessage();
        agentContext.setUserMessage(userMessage);

        log.info("设置用户消息: {}", userMessage);

        List<Long> toolIds = userChatRequest.getToolIds();
        if (CollectionUtils.isNotEmpty(toolIds)) {
            List<ToolCallback> tools = this.functionToolFactory.getAllTools(toolIds);
            log.info("设置自定义工具: toolIds={}, 工具数量={}", toolIds, tools.size());
            agentContext.setToolCallbacks(tools);
        } else {
            List<ToolCallback> builtinTools = this.functionToolFactory.getBuiltinTools();
            log.info("设置内置工具: 工具数量={}", builtinTools.size());
            agentContext.setToolCallbacks(builtinTools);
        }

        // 根据模型配置创建 ChatClient
        ChatClient chatClient = this.dynamicChatClient.createFromModelConfig(modelConfig);
        log.info("创建 ChatClient 成功");

        // 异步处理 LLM 调用，避免阻塞订阅
        CompletableFuture.runAsync(() -> {
            try {
                ReActLLM reActLLM = new DataCollectorAgent(chatClient, agentContext);
                reActLLM.run(userMessage);
            } catch (Exception e) {
                agentContext.error(e);
                sink.tryEmitError(e);
            }
        }, this.executor);

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
        if (StrUtil.isNotEmpty(form.getSessionId())) {
            return form.getSessionId();
        }
        return "sessionId-" + UUID.randomUUID();
    }

    /**
     * 生成请求 ID
     * <p>
     * 优先使用表单中的 requestId，否则生成新的 UUID
     *
     * @param form 聊天请求表单
     * @return 请求 ID
     * @since 1.0.0-SNAPSHOT
     */
    private String generateRequestId(UserChatRequest form) {
        if (StrUtil.isNotEmpty(form.getRequestId())) {
            return form.getRequestId();
        }
        return "requestId-" + UUID.randomUUID();
    }
}
