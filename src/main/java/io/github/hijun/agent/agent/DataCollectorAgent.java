package io.github.hijun.agent.agent;

import io.github.hijun.agent.common.constant.AgentConstants;
import io.github.hijun.agent.entity.AgentContext;
import org.springframework.ai.chat.client.ChatClient;

/**
 * Data Collector Agent
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/2/2 18:24
 * @since 1.0.0-SNAPSHOT
 */
public class DataCollectorAgent extends ReActLLM {
    /**
     * ReAct LLM
     *
     * @param chatClient   chat client
     * @param agentContext agent context
     * @since 1.0.0-SNAPSHOT
     */
    public DataCollectorAgent(ChatClient chatClient,
                              AgentContext agentContext) {
        super(chatClient,
                AgentConstants.ReAct.SYSTEM_PROMPT,
                AgentConstants.ReAct.NEXT_STEP_PROMPT,
                agentContext);
    }
}
