package io.github.hijun.agent.agent;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.github.hijun.agent.annotations.Agent;
import io.github.hijun.agent.common.constant.AgentConstants;
import io.github.hijun.agent.entity.AgentContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage.ToolCall;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Multi Agent
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/2/3 11:33
 * @since 1.0.0-SNAPSHOT
 */
@Slf4j
public class MultiCollaborationAgent extends ReActLLM {


    /**
     * agents.
     */
    private final List<ReActLLM> agents = new LinkedList<>();

    /**
     * ReAct LLM
     *
     * @param chatClient   chat client
     * @param agentContext agent context
     * @since 1.0.0-SNAPSHOT
     */
    public MultiCollaborationAgent(ChatClient chatClient,
                                   AgentContext agentContext) {
        super(chatClient,
                AgentConstants.MultiCollaboration.SYSTEM_PROMPT,
                AgentConstants.MultiCollaboration.NEXTSTEP_PROMPT,
                agentContext);

        this.agents.add(new DataCollectorAgent(chatClient, agentContext));
    }

    /**
     * Get Agent List
     *
     * @return string
     * @since 1.0.0-SNAPSHOT
     */
    private String getAgentList() {
        StringJoiner joiner = new StringJoiner("\n");
        this.agents.forEach(agent -> {
            Agent annotation = AnnotationUtil.getAnnotation(agent.getClass(), Agent.class);
            if (annotation != null) {
                joiner.add(annotation.value() + ":" + annotation.description());
            }
        });
        return joiner.toString();
    }

    /**
     * Get Template Context
     *
     * @return map
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    protected Map<String, Object> getTemplateContext() {
        Map<String, Object> context = super.getTemplateContext();
        context.put("AgentList", this.getAgentList());
        context.put("concurrentTime", DateUtil.now());
        return context;
    }

    /**
     * Think
     *
     * @param messages messages
     * @return list
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    protected List<ToolCall> think(List<Message> messages) {
        AgentCallback agentCallback = this.callLLM(messages, this.agentContext.getToolCallbacks(), false, AgentCallback.class);
        if (agentCallback == null || agentCallback.agentId() == null) {
            return Collections.emptyList();
        }
        return List.of(new ToolCall(agentCallback.agentId(), "Agent", agentCallback.name(), agentCallback.input()));
    }

    /**
     * Action
     *
     * @param toolCalls tool calls
     * @return list
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    protected List<ToolResponseMessage.ToolResponse> action(List<ToolCall> toolCalls) {
        if (CollUtil.isEmpty(toolCalls)) {
            return Collections.emptyList();
        }
        ToolCall agentCall = toolCalls.get(0);

        // 调用agent

        return List.of(new ToolResponseMessage.ToolResponse(agentCall.id(), agentCall.name(), ""));
    }

    /**
     * Agent Callback
     *
     * @author haijun
     * @version 1.0.0-SNAPSHOT
     * @email "mailto:iamxiaohaijun@gmail.com"
     * @date 2026/2/4 11:18
     * @since 1.0.0-SNAPSHOT
     */
    @JsonClassDescription("指定Agent调用响应")
    public record AgentCallback(@JsonPropertyDescription("指定") String agentId,
                                @JsonPropertyDescription("Agent名称") String name,
                                @JsonPropertyDescription("输入任务参数") String input) {
    }
}
