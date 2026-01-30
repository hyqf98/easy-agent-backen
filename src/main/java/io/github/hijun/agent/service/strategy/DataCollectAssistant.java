package io.github.hijun.agent.service.strategy;

import io.github.hijun.agent.common.Agent;
import io.github.hijun.agent.common.enums.SseMessageType;
import io.github.hijun.agent.common.enums.ToolStatus;
import io.github.hijun.agent.entity.dto.ContentMessage;
import io.github.hijun.agent.entity.dto.ToolMessage;
import io.github.hijun.agent.entity.po.AgentContext;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 数据采集智能体
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/1/13 14:13
 * @since 1.0.0-SNAPSHOT
 */
@Agent(id = "10001", name = "DataCollectAssistant", description = "数据采集助手，善于利用各种工具进行数据收集")
public class DataCollectAssistant extends BaseAgent {


    /**
     * React Agent
     *
     * @param chatClient chat client
     * @since 3.4.3
     */
    public DataCollectAssistant(ChatClient chatClient) {
        super(chatClient);
    }

    /**
     * Think
     *
     * @param agentContext agent context
     * @return boolean
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    protected boolean think(AgentContext agentContext) {
        List<Message> memory = agentContext.getMemory();
        Flux<ChatResponse> responseFlux = this.callLLM(memory, agentContext.getToolCallbacks(), false);
        // 发送思考数据
        return Boolean.TRUE.equals(responseFlux.doOnNext(chatResponse -> {
                    if (chatResponse == null) {
                        return;
                    }
                    if (chatResponse.hasToolCalls()) {
                        List<AssistantMessage.ToolCall> toolCalls = chatResponse.getResults()
                                .stream()
                                .map(generation -> generation.getOutput().getToolCalls())
                                .flatMap(List::stream)
                                .toList();
                        agentContext.setObserveTools(toolCalls);
                    }
                    Generation result = chatResponse.getResult();
                    AssistantMessage assistantMessage = result.getOutput();
                    String text = assistantMessage.getText();
                    if (StringUtils.hasText(text)) {
                        // 发送思考数据
                        ContentMessage thinkMessage = ContentMessage.builder()
                                .content(text)
                                .type(SseMessageType.THINKING)
                                .build();
                        agentContext.sendMessage(thinkMessage);
                    }
                }).last()
                .map(chatResponse -> agentContext.hasTools())
                .defaultIfEmpty(Boolean.FALSE)
                .block());
    }

    /**
     * Action
     *
     * @param agentContext agent context
     * @return string
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    protected String action(AgentContext agentContext) {
        List<AssistantMessage.ToolCall> observeTools = agentContext.getObserveTools();

        List<CompletableFuture<ToolResponseMessage.ToolResponse>> completableFutures = observeTools.stream().map(toolCall ->
                CompletableFuture.supplyAsync(() -> {
                    ToolResponseMessage.ToolResponse toolResponse = this.callTool(agentContext, toolCall);
                    ToolMessage toolMessage = ToolMessage.builder()
                            .id(toolCall.id())
                            .name(toolCall.name())
                            .toolStatus(ToolStatus.SUCCESS)
                            .result(toolResponse.responseData())
                            .build();
                    agentContext.sendMessage(toolMessage);
                    return toolResponse;
                })).toList();

        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).join();
        List<ToolResponseMessage.ToolResponse> toolCallResult = completableFutures
                .stream()
                .map(CompletableFuture::join).toList();

        ToolResponseMessage toolResponseMessage = ToolResponseMessage.builder()
                .responses(toolCallResult)
                .build();
        agentContext.updateMemory(toolResponseMessage);
        return "";
    }
}
