package io.github.hijun.agent.service.strategy;

import io.github.hijun.agent.entity.po.AgentContext;
import io.github.hijun.agent.entity.po.CallResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;

/**
 * 简单智能体基类.
 * <p>用于不需要 ReAct 循环的简单任务执行</p>
 *
 * @author hijun
 * @since 1.0.0
 */
@Slf4j
public abstract class SimpleAgent extends BaseLLM<CallResponse> {

    /**
     * 构造函数.
     *
     * @param chatClient 聊天客户端
     */
    public SimpleAgent(ChatClient chatClient) {
        super(chatClient);
    }

    /**
     * 执行智能体任务.
     *
     * @param context 智能体上下文
     * @return 调用响应
     */
    @Override
    public final CallResponse run(AgentContext context) {
        log.info("执行智能体任务: {}", this.getClass().getSimpleName());
        try {
            return execute(context);
        } catch (Exception e) {
            log.error("智能体执行失败", e);
            return CallResponse.builder()
                    .success(false)
                    .message("执行失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 执行具体的智能体逻辑.
     * <p>子类实现此方法来定义具体的执行逻辑</p>
     *
     * @param context 智能体上下文
     * @return 调用响应
     */
    protected abstract CallResponse execute(AgentContext context);

    /**
     * 获取系统提示词.
     *
     * @return 系统提示词
     */
    protected abstract String getSystemPrompt();
}
