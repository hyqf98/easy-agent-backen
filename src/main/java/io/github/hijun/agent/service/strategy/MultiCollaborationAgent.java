package io.github.hijun.agent.service.strategy;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.github.hijun.agent.common.enums.AgentStatus;
import io.github.hijun.agent.config.AgentProperties;
import io.github.hijun.agent.entity.dto.ContentMessage;
import io.github.hijun.agent.entity.po.AgentContext;
import io.github.hijun.agent.utils.JSONS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage.ToolResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.template.TemplateRenderer;
import org.springframework.ai.template.ValidationMode;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * React Impl Agent
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/1/5 15:15
 * @since 1.0.0-SNAPSHOT
 */
@Slf4j
@Service
public class MultiCollaborationAgent extends BaseLLM<MultiCollaborationAgent.FinalResult> {

    /**
     * system prompt.
     */
    public static final String SYSTEM_PROMPT = """
            # 角色: ReAct 架构多智能体编排器 (ReAct Multi-Agent Orchestrator)
            
            ## 职责
            你是一个拥有极强逻辑推理能力的智能体编排器。你的运作方式不是一次性生成所有答案，而是基于 **ReAct (Reasoning + Acting)** 策略，通过多轮迭代来解决问题。
            你需要动态判断当前进度，每次只调用一个最合适的“虚拟专家”执行具体步骤，观察其结果，然后再决定下一步行动，直到彻底解决用户问题。
            
            ## 核心职责
            1.  **Iterative (迭代式)**: 不要试图一步到位。通过一连串的步骤逐步逼近最终答案。
            2.  **Dynamic (动态性)**: 下一步调用哪个专家，完全取决于上一步的结果。
            3.  **Single-Threaded (单线程)**: 每次只激活一个专家，保持上下文清晰。
            
            ## 工作流 (ReAct Loop)
            针对用户的问题，请严格按照以下循环格式进行输出，直到任务结束：
            
            ### 1. [THOUGHT] (思考)
            * 分析当前掌握的信息。
            * 判断还需要什么信息或操作才能推进任务。
            * 决定**当下**最需要哪种专家（例如：需要查资料、写代码、润色文本、还是逻辑检查）。
            
            ### 2. [ACT] (行动 - 调用专家)
            * 定义专家的角色名称（例如：`WebSearcher`, `PythonExpert`, `CreativeWriter`）。
            * 向该专家下达具体的任务指令，你可以将上一个**专家**输出的内容进行总结传递给下一个**专家**作为上下文信息使用，传递的可以是**文件链接、纯文本数据、JSON格式数据**等。
            
            ## 可用专家列表
            {experts}
            
            ## 用户文件列表
            {userFile}
            
            ## 系统环境变量
            当前系统时间:{time}
            语言环境:{language}
            """;

    /**
     * next prompt.
     */
    public static final String NEXT_PROMPT = """
            接下来参考智能体专家返回的数据，判断用户的需求是否可以完成
            
            用户的需求任务
            <{userQuery}>
            
            调用智能体专家返回数据
            {expertResult}
            """;

    /**
     * s u m m a r y.
     */
    public static final String SUMMARY = """
            现在对上下文中提供的数据进行总结，要求语句简短、精炼，200字以内，并且将上下文中涉及到相关的文件链接地址根据文件重要性进行排序后提取；
            总结的内容不要出现任何内部智能体专家的名称、工具名称等敏感信息。
            如果上下文中信息为简单的问题，请对用户的信息进行友好的回答
            """;

    /**
     * template renderer.
     */
    private final TemplateRenderer templateRenderer;


    /**
     * agent properties.
     */
    private final AgentProperties agentProperties;

    /**
     * agent manager.
     */
    private final AgentManager agentManager;

    /**
     * React Agent
     *
     * @param chatClient      chat client
     * @param agentProperties agent properties
     * @param agentManager    agent manager
     * @since 3.4.3
     */
    public MultiCollaborationAgent(ChatClient chatClient,
                                   AgentProperties agentProperties,
                                   AgentManager agentManager) {
        super(chatClient);
        this.agentProperties = agentProperties;
        this.agentManager = agentManager;
        this.templateRenderer = new StTemplateRenderer('{',
                '}',
                ValidationMode.WARN,
                true);
    }

    /**
     * Run
     *
     * @param agentContext agent context
     * @return final result
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public FinalResult run(AgentContext agentContext) {
        Integer maxStep = this.agentProperties.getMaxStep();
        String userPrompt = agentContext.getUserPrompt();
        if (StringUtils.hasText(userPrompt)) {
            UserMessage userMessage = UserMessage.builder()
                    .text(userPrompt)
                    .build();
            agentContext.updateMemory(userMessage);
        }
        String userQuery = agentContext.getUserQuery();
        if (StrUtil.isBlank(userQuery)) {
            return new FinalResult("请输入您需要询问的问题", List.of());
        }
        UserMessage userMessage = UserMessage.builder()
                .text(userQuery)
                .build();
        agentContext.updateMemory(userMessage);

        agentContext.setAgentStatus(AgentStatus.RUNNING);
        // 构建循环
        while (agentContext.getConcurrentStep() < maxStep && agentContext.getAgentStatus() == AgentStatus.RUNNING) {
            try {
                agentContext.incrementConcurrentStep();
                if (!agentContext.lastMessageIsUser()) {
                    UserMessage nextMessage = UserMessage.builder()
                            .text(this.templateRenderer.apply(NEXT_PROMPT, Map.of("userQuery", userQuery)))
                            .build();
                    agentContext.updateMemory(nextMessage);
                }
                List<Message> memory = agentContext.getMemory();
                AgentCall agentCall = this.callLLM(memory,
                        agentContext.getToolCallbacks(),
                        false,
                        AgentCall.class);
                if (agentCall != null) {
                    String agentName = agentCall.agentName();
                    String agentId = agentCall.agentId();
                    AgentContext copyAgent = this.copyContext(agentCall, agentContext);
                    Object agentResult = this.agentManager.callAgent(agentId, agentName, copyAgent);
                    if (agentResult != null) {
                        ToolResponse toolResponse = new ToolResponse(agentId,
                                agentName,
                                agentResult.toString());
                        ToolResponseMessage toolResponseMessage =
                                ToolResponseMessage.builder()
                                        .responses(List.of(toolResponse))
                                        .build();
                        agentContext.updateMemory(toolResponseMessage);
                    }
                } else {
                    agentContext.setAgentStatus(AgentStatus.FINISHED);
                }
            } catch (Exception e) {
                agentContext.setAgentStatus(AgentStatus.ERROR);
                agentContext.getSseEmitter().completeWithError(e);
                return new FinalResult("智能体运行过程中出现了问题，请联系管理员！！！", List.of());
            }
        }
        if (agentContext.getAgentStatus() == AgentStatus.FINISHED) {
            List<Message> memory = agentContext.getMemory();
            memory.add(UserMessage.builder().text(SUMMARY).build());
            FinalResult finalResult = this.callLLM(memory,
                    agentContext.getToolCallbacks(),
                    false,
                    FinalResult.class);
            agentContext.sendMessage(ContentMessage.builder().content(JSONS.toJson(finalResult)).build());
            agentContext.getSseEmitter().complete();
            return finalResult;
        }
        agentContext.getSseEmitter().complete();
        return null;
    }

    /**
     * Copy Context
     *
     * @param agentCall    agent call
     * @param agentContext agent context
     * @return agent context
     * @since 1.0.0-SNAPSHOT
     */
    private AgentContext copyContext(AgentCall agentCall, AgentContext agentContext) {

        AgentContext copyAgentContext = AgentContext.builder()
                .sessionId(agentContext.getSessionId())
                .requestId(agentContext.getRequestId())
                .chatMode(agentContext.getChatMode())
                .userQuery(agentCall.task())
                .userPrompt(agentContext.getUserPrompt())
                .userUploadFiles(agentContext.getUserUploadFiles())
                .sseEmitter(agentContext.getSseEmitter())
                .toolCallbacks(agentContext.getToolCallbacks())
                .build();
        String context = agentCall.context();
        if (StringUtils.hasText(context)) {
            UserMessage userMessage = UserMessage.builder()
                    .text(context)
                    .build();
            copyAgentContext.updateMemory(userMessage);
        }
        return copyAgentContext;
    }


    /**
     * Get Context Params
     *
     * @return map
     * @since 1.0.0-SNAPSHOT
     */
    private Map<String, Object> getContextParams() {
        return Map.of(
                "time", DateUtil.formatDateTime(DateUtil.date()),
                "language", "zh-CN",
                "experts", this.agentManager.getAgentDescription()
        );
    }

    /**
     * Get System Prompt
     *
     * @return string
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    protected String getSystemPrompt() {
        return this.templateRenderer.apply(SYSTEM_PROMPT, this.getContextParams());
    }

    /**
     * Final Result
     *
     * @author haijun
     * @version 1.0.0-SNAPSHOT
     * @email "mailto:iamxiaohaijun@gmail.com"
     * @date 2026/1/13 12:55
     * @since 1.0.0-SNAPSHOT
     */
    @JsonClassDescription("最终结果实体")
    public record FinalResult(@JsonPropertyDescription("最终总结结果说明") String content,
                              @JsonPropertyDescription("文件信息") List<String> fileInfo) {
    }

    /**
     * Agent Call
     *
     * @author haijun
     * @version 1.0.0-SNAPSHOT
     * @email "mailto:iamxiaohaijun@gmail.com"
     * @date 2026/1/13 13:22
     * @since 1.0.0-SNAPSHOT
     */
    @JsonClassDescription("Agent专家调用响应实体")
    public record AgentCall(
            @JsonPropertyDescription("专家ID") String agentId,
            @JsonPropertyDescription("专家名称") String agentName,
            @JsonPropertyDescription("专家调用任务问题") String task,
            @JsonPropertyDescription("专家上下文信息") String context) {

    }
}
