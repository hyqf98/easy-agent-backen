package io.github.hijun.agent.service.strategy;

import cn.hutool.core.util.StrUtil;
import io.github.hijun.agent.common.enums.AgentStatus;
import io.github.hijun.agent.common.enums.SseMessageType;
import io.github.hijun.agent.entity.dto.ContentMessage;
import io.github.hijun.agent.entity.po.AgentContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;

/**
 * React Agent
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:zhonghaijun@zhxx.com"
 * @date 2025/12/30 13:30
 * @since 3.4.3
 */
@Data
@Slf4j
public abstract class BaseAgent {

    /**
     * max tool call depth.
     */
    private static final Integer MAX_TOOL_CALL_DEPTH = 30;

    /**
     * max step.
     */
    private Integer maxStep;

    /**
     * chat client.
     */
    protected final ChatClient chatClient;

    /**
     * React Agent
     *
     * @param chatClient chat client
     * @since 3.4.3
     */
    public BaseAgent(ChatClient chatClient) {
        this.chatClient = chatClient;
    }


    /**
     * Do Chat
     *
     * @param agentContext agent context
     * @return sse emitter
     * @since 1.0.0-SNAPSHOT
     */
    public AgentContext run(AgentContext agentContext) {
        agentContext.setAgentStatus(AgentStatus.IDLE);
        try {
            String userQuery = agentContext.getUserQuery();
            if (StrUtil.isNotBlank(userQuery)) {
                UserMessage userMessage = UserMessage
                        .builder()
                        .text(userQuery)
                        .build();
                agentContext.updateMemory(userMessage);
            }
            while (agentContext.getConcurrentStep() < MAX_TOOL_CALL_DEPTH && agentContext.getAgentStatus() != AgentStatus.FINISHED) {
                agentContext.incrementConcurrentStep();
                // 判断一下最后一条数据是否是用户数据，不是用户数据则构建下一步
                if (!agentContext.lastMessageIsUser()) {
                    UserMessage userMessage = UserMessage.builder()
                            .text(this.getDefaultNextStepPrompt())
                            .build();
                    agentContext.updateMemory(userMessage);
                }
                this.step(agentContext);
            }
            // 对结果进行总结
            this.observe(agentContext);
            agentContext.setAgentStatus(AgentStatus.FINISHED);
            agentContext.complete();
        } catch (Exception e) {
            agentContext.setAgentStatus(AgentStatus.ERROR);
            ContentMessage contentMessage = ContentMessage.builder()
                    .content("智能体调用异常:" + e.getMessage())
                    .type(SseMessageType.ERROR)
                    .build();
            agentContext.sendMessage(contentMessage);
            agentContext.complete();
        }
        return agentContext;
    }


    /**
     * Step
     *
     * @param agentContext agent context
     * @return string
     * @since 3.4.3
     */
    private String step(AgentContext agentContext) {
        boolean shouldAct = this.think(agentContext);
        if (!shouldAct) {
            agentContext.setAgentStatus(AgentStatus.FINISHED);
            return "Thinking complete - no action needed";
        }
        return this.action(agentContext);
    }

    /**
     * 思考
     *
     * @param agentContext agent context
     * @return boolean
     * @since 3.4.3
     */
    protected abstract boolean think(AgentContext agentContext);

    /**
     * 行动
     *
     * @param agentContext agent context
     * @return string
     * @since 3.4.3
     */
    protected abstract String action(AgentContext agentContext);

    /**
     * 观察结果
     *
     * @param agentContext agent context
     * @since 3.4.3
     */
    protected abstract void observe(AgentContext agentContext);

    /**
     * Get Default System Prompt
     *
     * @return string
     * @since 3.4.3
     */
    protected String getDefaultSystemPrompt() {
        return "You are a helpful assistant.";
    }

    /**
     * Get Default Next Step Prompt
     *
     * @return string
     * @since 3.4.3
     */
    protected String getDefaultNextStepPrompt() {
        return "You are a helpful assistant.";
    }
}
