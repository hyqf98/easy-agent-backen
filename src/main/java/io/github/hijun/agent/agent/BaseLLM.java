package io.github.hijun.agent.agent;

import cn.hutool.core.util.StrUtil;
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
     * Base L L M
     *
     * @param chatClient chat client
     * @since 1.0.0-SNAPSHOT
     */
    public BaseLLM(ChatClient chatClient) {
        this.chatClient = chatClient;
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
        if (clazz == String.class) {
            return (T) blockedLast;
        }
        BeanOutputConverter<T> outputConverter = new BeanOutputConverter<>(clazz);
        return outputConverter.convert(blockedLast);
    }
}
