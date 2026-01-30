package io.github.hijun.agent.agent;

import io.github.hijun.agent.common.enums.AgentStatus;
import io.github.hijun.agent.entity.AgentContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;

/**
 * ReAct LLM
 * <p>
 * 实现 ReAct (Reasoning + Acting) 模式的 LLM Agent
 * 支持循环调度，最多执行 30 步
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:zhonghaijun@zhxx.com"
 * @date 2026.01.30 18:14
 * @since 1.0.0-SNAPSHOT
 */
@Slf4j
public abstract class ReActLLM extends BaseLLM {

    /**
     * ReAct LLM
     *
     * @param chatClient chat client
     * @since 1.0.0-SNAPSHOT
     */
    public ReActLLM(ChatClient chatClient) {
        super(chatClient);
    }

    /**
     * 循环执行 ReAct 流程
     * <p>
     * ReAct 循环: Think -> Act -> Observe -> Think ...
     * 最多执行 30 步，直到达到停止条件
     *
     * @param agentContext Agent 上下文
     * @since 1.0.0-SNAPSHOT
     */
    public void loop(AgentContext agentContext) {
        agentContext.setStatus(AgentStatus.RUNNING);
        log.info("开始 ReAct 循环，会话ID: {}", agentContext.getSessionId());

        try {
            // 循环执行直到达到停止条件
            while (agentContext.isMaxStepsReached()) {
                // 增加步数
                int step = agentContext.incrementStep();
                log.debug("ReAct 步骤: {}/{}", step, agentContext.getCurrentStep());

                // 思考阶段
                if (!this.think(agentContext)) {
                    log.info("思考阶段返回 false，停止循环");
                    break;
                }

                // 行动阶段
                this.action(agentContext);
            }

            // 完成执行
            log.info("ReAct 循环完成，总步数: {}", agentContext.getCurrentStep());
            agentContext.complete();

        } catch (Exception e) {
            log.error("ReAct 循环发生错误: {}", e.getMessage(), e);
            agentContext.error(e);
        }
    }

    /**
     * 思考阶段
     * <p>
     * 子类需要实现具体的思考逻辑
     * 返回 false 表示应该停止循环
     *
     * @param agentContext Agent 上下文
     * @return true 继续执行，false 停止执行
     * @since 1.0.0-SNAPSHOT
     */
    public boolean think(AgentContext agentContext) {

        return false;
    }

    /**
     * 行动阶段
     * <p>
     * 子类需要实现具体的行动逻辑
     *
     * @param agentContext Agent 上下文
     * @since 1.0.0-SNAPSHOT
     */
    public void action(AgentContext agentContext) {
        return;
    }
}
