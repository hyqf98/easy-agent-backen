package io.github.hijun.agent.service.strategy;

import io.github.hijun.agent.entity.po.AgentContext;
import org.springframework.ai.chat.client.ChatClient;

/**
 * Report Assistant
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/1/9 18:22
 * @since 1.0.0-SNAPSHOT
 */
public class ReportAssistant extends BaseLLM<String> {
    /**
     * Base L L M
     *
     * @param chatClient chat client
     * @since 1.0.0-SNAPSHOT
     */
    public ReportAssistant(ChatClient chatClient) {
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
        return "报告写入文件:" + "文件请求地址:";
    }
}
