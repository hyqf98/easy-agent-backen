package io.github.hijun.agent.service.strategy;

import io.github.hijun.agent.common.Agent;
import io.github.hijun.agent.entity.po.AgentContext;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * Report Assistant
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/1/9 18:22
 * @since 1.0.0-SNAPSHOT
 */
@Agent(id = "10002",
        name = "ReportAssistant",
        description = """
                报告生成助手，善于根据上下文提供的文件信息与数据信息，对用户的任务进行总结并且生成对应的报告文件
                """)
@Component
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
