package io.github.hijun.agent.agent;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.template.TemplateRenderer;
import org.springframework.ai.template.ValidationMode;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Flux;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Base L L M
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/1/9 13:29
 * @since 1.0.0-SNAPSHOT
 */
@Slf4j
public abstract class BaseLLM {


    /**
     * chat client.
     */
    protected final ChatClient chatClient;

    /**
     * template renderer.
     */
    protected final TemplateRenderer templateRenderer;

    /**
     * Base L L M
     *
     * @param chatClient chat client
     * @since 1.0.0-SNAPSHOT
     */
    public BaseLLM(ChatClient chatClient) {
        this(chatClient, new StTemplateRenderer('{',
                '}',
                ValidationMode.WARN,
                false));
    }

    /**
     * Base L L M
     *
     * @param chatClient       chat client
     * @param templateRenderer template renderer
     * @since 1.0.0-SNAPSHOT
     */
    public BaseLLM(ChatClient chatClient, TemplateRenderer templateRenderer) {
        this.chatClient = chatClient;
        this.templateRenderer = templateRenderer;
    }

    /**
     * CallLLM
     *
     * @param messages       messages
     * @param toolCallbacks  tool callbacks
     * @param enableToolCall enable tool call
     * @return flux
     * @since 1.0.0-SNAPSHOT
     */
    public Flux<ChatResponse> callLLM(List<Message> messages,
                                      List<ToolCallback> toolCallbacks,
                                      boolean enableToolCall) {

        ToolCallingChatOptions callingChatOptions = ToolCallingChatOptions.builder()
                .internalToolExecutionEnabled(enableToolCall)
                .toolCallbacks(toolCallbacks)
                .build();

        return this.chatClient.prompt()
                .options(callingChatOptions)
                .messages(messages)
                .templateRenderer(this.templateRenderer)
                .stream()
                .chatResponse()
                .doOnError(throwable -> log.error("LLM call error: {}", throwable.getMessage()))
                .doOnComplete(() -> log.info("LLM call complete"));
    }

    /**
     * Call L L M
     *
     * @param <T>            类型参数 T
     * @param messages       messages
     * @param toolCallbacks  tool callbacks
     * @param enableToolCall enable tool call
     * @param clazz          clazz
     * @return t
     * @since 1.0.0-SNAPSHOT
     */
    public <T> T callLLM(List<Message> messages,
                         List<ToolCallback> toolCallbacks,
                         boolean enableToolCall,
                         Class<T> clazz) {

        ToolCallingChatOptions callingChatOptions = ToolCallingChatOptions.builder()
                .internalToolExecutionEnabled(enableToolCall)
                .toolCallbacks(toolCallbacks)
                .build();

        return this.chatClient
                .prompt()
                .options(callingChatOptions)
                .templateRenderer(this.templateRenderer)
                .messages(messages)
                .call()
                .entity(clazz);
    }

    /**
     * Call L L M
     *
     * @param <T>            类型参数 T
     * @param messages       messages
     * @param toolCallbacks  tool callbacks
     * @param enableToolCall enable tool call
     * @param clazz          clazz
     * @return t
     * @since 1.0.0-SNAPSHOT
     */
    public <T> T callLLM(List<Message> messages,
                         List<ToolCallback> toolCallbacks,
                         boolean enableToolCall,
                         TypeReference<T> clazz) {

        ToolCallingChatOptions callingChatOptions = ToolCallingChatOptions.builder()
                .internalToolExecutionEnabled(enableToolCall)
                .toolCallbacks(toolCallbacks)
                .build();

        return this.chatClient
                .prompt()
                .options(callingChatOptions)
                .templateRenderer(this.templateRenderer)
                .messages(messages)
                .call()
                .entity(new ParameterizedTypeReference<>() {
                    @Override
                    public Type getType() {
                        return clazz.getType();
                    }
                });
    }

    /**
     * Get Template Context
     *
     * @return map
     * @since 1.0.0-SNAPSHOT
     */
    protected Map<String, Object> getTemplateContext() {
        return Map.of();
    }
}
