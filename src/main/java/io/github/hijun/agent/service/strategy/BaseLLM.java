package io.github.hijun.agent.service.strategy;

import cn.hutool.core.util.StrUtil;
import io.github.hijun.agent.entity.po.AgentContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallback;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Base L L M
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:haijun@email.com"
 * @date 2026/1/9 13:29
 * @since 1.0.0-SNAPSHOT
 * @param <T> t
 */
@Slf4j
public abstract class BaseLLM<T> {


    /**
     * chat client.
     */
    protected final ChatClient chatClient;

    /**
     * Base L L M
     *
     * @param chatClient chat client
     * @since 1.0.0-SNAPSHOT
     */
    public BaseLLM(ChatClient chatClient) {
        this.chatClient = chatClient;
    }


    /**
     * Run
     *
     * @param agentContext agent context
     * @return t
     * @since 1.0.0-SNAPSHOT
     */
    public abstract T run(AgentContext agentContext);

    /**
     * Call L L M
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
                .system(this.getSystemPrompt())
                .messages(messages)
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

        Flux<ChatResponse> flux = this.callLLM(messages, toolCallbacks, enableToolCall);
        ChatResponse chatResponse = flux.blockLast();
        if (chatResponse == null) {
            return null;
        }
        String blockedLast = chatResponse.getResult().getOutput().getText();
        if (StrUtil.isBlank(blockedLast)) {
            return null;
        }
        BeanOutputConverter<T> outputConverter = new BeanOutputConverter<>(clazz);
        return outputConverter.convert(blockedLast);
    }

    /**
     * Get System Prompt
     *
     * @return string
     * @since 1.0.0-SNAPSHOT
     */
    protected String getSystemPrompt() {
        return "";
    }
}
