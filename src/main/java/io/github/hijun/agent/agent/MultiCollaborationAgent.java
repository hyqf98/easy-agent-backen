package io.github.hijun.agent.agent;

import io.github.hijun.agent.entity.AgentContext;
import org.springframework.ai.chat.client.ChatClient;

/**
 * Multi Agent
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/2/3 11:33
 * @since 1.0.0-SNAPSHOT
 */
public class MultiCollaborationAgent extends ReActLLM {
    /**
     * ReAct LLM
     *
     * @param chatClient   chat client
     * @param agentContext agent context
     * @since 1.0.0-SNAPSHOT
     */
    public MultiCollaborationAgent(ChatClient chatClient,
                                   AgentContext agentContext) {
        super(chatClient, "", "", agentContext);
    }
}
