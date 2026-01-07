package io.github.hijun.agent.service.strategy;

import cn.hutool.core.date.DateUtil;
import io.github.hijun.agent.common.enums.SseMessageType;
import io.github.hijun.agent.config.SystemPromptProperties;
import io.github.hijun.agent.config.SystemPromptProperties.AgentConfig;
import io.github.hijun.agent.entity.dto.ContentMessage;
import io.github.hijun.agent.entity.po.AgentContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage.ToolResponse;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.template.TemplateRenderer;
import org.springframework.ai.template.ValidationMode;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.resolution.StaticToolCallbackResolver;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * React Impl Agent
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:haijun@email.com"
 * @date 2026/1/5 15:15
 * @since 1.0.0-SNAPSHOT
 */
@Slf4j
@Service
public class ReactAgent extends BaseAgent {

    /**
     * react agent config.
     */
    private final AgentConfig reactAgentConfig;

    /**
     * summary agent config.
     */
    private final AgentConfig summaryAgentConfig;

    /**
     * template renderer.
     */
    private final TemplateRenderer templateRenderer;

    /**
     * tool callback resolver.
     */
    private final ToolCallbackResolver toolCallbackResolver;

    /**
     * tool callbacks.
     */
    private final List<ToolCallback> toolCallbacks;

    /**
     * React Agent
     *
     * @param chatClient         chat client
     * @param systemPromptProperties system prompt config
     * @since 3.4.3
     */
    public ReactAgent(ChatClient chatClient,
                      SystemPromptProperties systemPromptProperties) {
        super(chatClient);
        this.reactAgentConfig = systemPromptProperties.getReact();
        this.summaryAgentConfig = systemPromptProperties.getSummary();
        super.setMaxStep(this.reactAgentConfig.getMaxStep());
        this.templateRenderer = new StTemplateRenderer('{',
                '}',
                ValidationMode.WARN,
                true);

        // 从ToolService获取所有工具回调
        this.toolCallbacks = List.of();
        this.toolCallbackResolver = new StaticToolCallbackResolver(this.toolCallbacks);
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
        ToolCallingChatOptions toolCallingChatOptions = ToolCallingChatOptions.builder()
                .internalToolExecutionEnabled(Boolean.FALSE)
                .toolCallbacks(this.toolCallbacks)
                .build();
        String systemPrompt = this.reactAgentConfig.getSystemPrompt();
        ChatClient.ChatClientRequestSpec requestSpec = this.chatClient
                .prompt()
                .system(systemSpec -> systemSpec.text(systemPrompt)
                        .params(this.getContextParams()))
                .messages(agentContext.getMemory())
                .options(toolCallingChatOptions)
                .templateRenderer(this.templateRenderer);
        Flux<ChatResponse> responseFlux = requestSpec.stream().chatResponse();
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
        List<CompletableFuture<ToolResponse>> allToolCallTask = observeTools.stream()
                .map(toolCall -> CompletableFuture.supplyAsync(() -> {
                    String id = toolCall.id();
                    String toolName = toolCall.name();
                    String arguments = toolCall.arguments();
                    try {
                        ToolCallback toolCallback = this.toolCallbackResolver.resolve(toolName);
                        if (!StringUtils.hasText(arguments)) {
                            log.warn("Tool call arguments are null or empty for tool: {}. Using empty JSON object as default.", toolName);
                            arguments = "{}";
                        }
                        if (toolCallback == null) {
                            log.warn("LLM may have adapted the tool name '{}', especially if the name was truncated due to length limits. If this is the case, you can customize the prefixing and processing logic using McpToolNamePrefixGenerator", toolName);
                            return null;
                        }
                        String result = toolCallback.call(arguments);
                        return new ToolResponse(id, toolName, result);
                    } catch (Exception e) {
                        log.error("Tool call error: {}", e.getMessage());
                        return new ToolResponse(id, toolName, e.getMessage());
                    }
                })).toList();
        CompletableFuture<Void> completableFuture = CompletableFuture.allOf(allToolCallTask.toArray(new CompletableFuture[0]));
        completableFuture.join();
        List<ToolResponse> toolCallResult = allToolCallTask.stream().map(callTask -> {
                    try {
                        return callTask.get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
        ToolResponseMessage toolResponseMessage = ToolResponseMessage.builder()
                .responses(toolCallResult)
                .build();
        agentContext.updateMemory(toolResponseMessage);
        return "";
    }

    /**
     * Observe
     *
     * @param agentContext agent context
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    protected void observe(AgentContext agentContext) {
        List<Message> memory = agentContext.getMemory();
        String systemPrompt = this.summaryAgentConfig.getSystemPrompt();
        Flux<ChatResponse> responseFlux = this.chatClient.prompt()
                .system(systemSpec -> {
                    systemSpec.text(systemPrompt)
                            .params(this.getContextParams())
                            .param("query", agentContext.getUserQuery());
                })
                .messages(memory)
                .stream()
                .chatResponse();
        responseFlux.doOnNext(chatResponse -> {
            if (chatResponse == null) {
                return;
            }
            String text = chatResponse.getResult().getOutput().getText();
            if (StringUtils.hasText(text)) {
                // 添加总结
                ContentMessage summaryMessage = ContentMessage.builder()
                        .content(text)
                        .type(SseMessageType.CONTENT_CHUNK)
                        .build();
                agentContext.sendMessage(summaryMessage);
            }
        }).blockLast();
    }

    /**
     * Get Context Params
     *
     * @return map
     * @since 1.0.0-SNAPSHOT
     */
    private Map<String, Object> getContextParams() {
        return Map.of(
                "time", DateUtil.formatDateTime(DateUtil.date()),
                "language", "zh-CN"
        );
    }

    /**
     * Get System Prompt
     *
     * @return string
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public String getDefaultSystemPrompt() {
        return this.reactAgentConfig.getSystemPrompt();
    }

    /**
     * Get Next Step Prompt
     *
     * @return string
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public String getDefaultNextStepPrompt() {
        return this.reactAgentConfig.getNextStepPrompt();
    }
}
