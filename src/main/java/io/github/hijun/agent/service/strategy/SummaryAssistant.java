package io.github.hijun.agent.service.strategy;

import io.github.hijun.agent.entity.po.AgentContext;
import org.springframework.ai.chat.client.ChatClient;

/**
 * Summary Assistant
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/1/9 16:44
 * @since 1.0.0-SNAPSHOT
 */
public class SummaryAssistant extends BaseLLM<String> {
    /**
     * Base L L M
     *
     * @param chatClient chat client
     * @since 1.0.0-SNAPSHOT
     */
    public SummaryAssistant(ChatClient chatClient) {
        super(chatClient);
    }

    /**
     * Run
     *
     * @param agentContext agent context
     * @return string
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public String run(AgentContext agentContext) {

        return "";
    }
}
