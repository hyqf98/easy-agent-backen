package io.github.hijun.agent.service.strategy;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.github.hijun.agent.common.constant.AgentConstants;
import io.github.hijun.agent.common.enums.AgentStatus;
import io.github.hijun.agent.common.enums.ChatMode;
import io.github.hijun.agent.config.AgentProperties;
import io.github.hijun.agent.entity.dto.ContentMessage;
import io.github.hijun.agent.entity.po.AgentContext;
import io.github.hijun.agent.entity.po.CallResponse;
import io.github.hijun.agent.utils.Jsons;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 多智能体协作编排器。
 *
 * <p>负责协调多个智能体的执行，支持两种运行模式：</p>
 * <ul>
 * <li><b>智能体链模式</b>：用于 PPT/Markdown/HTML 生成，按固定顺序执行智能体链</li>
 * <li><b>ReAct 模式</b>：用于智能对话，基于 ReAct (Reasoning + Acting) 策略动态调用智能体</li>
 * </ul>
 *
 * <h3>智能体链执行流程</h3>
 * <pre>
 * 用户请求
 * ↓
 * MultiCollaborationAgent.runAgentChain()
 * ↓
 * ┌─────────────────────────────────────────────┐
 * │  1. PlanningAgent                            │
 * │     输出: plan.md                            │
 * │     消息: THINKING, FILE_CREATED, PLAN_RESULT│
 * └─────────────────────────────────────────────┘
 * ↓
 * ┌─────────────────────────────────────────────┐
 * │  2. DataCollectAgent                         │
 * │     读取: plan.md                            │
 * │     输出: data.md                            │
 * │     消息: THINKING, TOOL_CALL_*              │
 * └─────────────────────────────────────────────┘
 * ↓
 * ┌─────────────────────────────────────────────┐
 * │  3. ContentGenAgent                          │
 * │     读取: plan.md, data.md                   │
 * │     输出: content.md                         │
 * │     消息: THINKING, CONTENT_CHUNK            │
 * └─────────────────────────────────────────────┘
 * ↓
 * FinalResult (content.md 路径 - 直接作为最终文件)
 * </pre>
 *
 * <h3>ReAct 模式工作流</h3>
 * <ol>
 * <li><b>[THOUGHT]</b> - 分析当前信息，判断需要哪种专家</li>
 * <li><b>[ACT]</b> - 调用相应专家执行任务</li>
 * <li><b>[OBSERVE]</b> - 观察结果，决定下一步行动</li>
 * <li>重复直到任务完成</li>
 * </ol>
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/1/29 17:52
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
        // 检查是否需要使用智能体链模式（PPT/Markdown/HTML/REPORT 模式）
        ChatMode chatMode = agentContext.getChatMode();
        if (chatMode == ChatMode.PPT || chatMode == ChatMode.MARKDOWN ||
            chatMode == ChatMode.HTML || chatMode == ChatMode.REPORT) {
            return this.runAgentChain(agentContext);
        }

        // 智能对话模式使用原有的 ReAct 模式
        return this.runReactMode(agentContext);
    }

    /**
     * 运行智能体链模式。
     * <p>用于 PPT/Markdown/HTML 等生成模式，按固定顺序执行智能体链。</p>
     *
     * <p>智能体执行顺序：PlanningAgent -> DataCollectAgent -> ContentGenAgent</p>
     * <p>每个智能体的输出文件会作为下一个智能体的输入。</p>
     *
     * @param agentContext 智能体上下文
     * @return 最终结果，包含最终文件路径信息
     * @since 1.0.0-SNAPSHOT
     */
    private FinalResult runAgentChain(AgentContext agentContext) {
        log.info("使用智能体链模式，模式: {}", agentContext.getChatMode());

        // 使用常量定义的智能体链
        List<String> agentChain = AgentConstants.AgentChain.REPORT_CHAIN;

        List<String> fileInfos = new ArrayList<>();
        try {
            // 依次执行智能体链
            for (String agentKey : agentChain) {
                // 获取并执行智能体
                SimpleAgent agent = (SimpleAgent) this.agentManager.getAgent(agentKey);
                if (agent == null) {
                    log.error("智能体不存在: {}", agentKey);
                    continue;
                }

                CallResponse response = agent.run(agentContext);

                if (!response.getSuccess()) {
                    log.error("智能体 {} 执行失败: {}", agentKey, response.getMessage());
                    this.sendError(agentContext, "智能体执行失败: " + response.getMessage());
                    return new FinalResult("智能体执行失败", List.of());
                }

                // 收集文件信息
                if (response.getData() != null) {
                    fileInfos.add(response.getData().toString());
                }
            }

            // 发送完成消息
            this.sendCompleted(agentContext, "所有任务已完成");
            agentContext.getSseEmitter().complete();

            // ContentGenAgent 输出的 content.md 直接作为最终文件
            return new FinalResult("内容生成完成", fileInfos);

        } catch (Exception e) {
            log.error("智能体链执行失败", e);
            this.sendError(agentContext, "执行失败: " + e.getMessage());
            return new FinalResult("执行失败", List.of());
        }
    }

    /**
     * 运行 ReAct 模式.
     * <p>用于智能对话模式，动态调用智能体</p>
     *
     * @param agentContext 智能体上下文
     * @return 最终结果
     * @since 1.0.0-SNAPSHOT
     */
    private FinalResult runReactMode(AgentContext agentContext) {
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
            agentContext.sendMessage(ContentMessage.builder().content(Jsons.toJson(finalResult)).build());
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

    /**
     * 提取智能体短名称.
     *
     * @param fullName 完整名称（如 20001_PlanningAgent）
     * @return 短名称（如 PlanningAgent）
     * @since 1.0.0-SNAPSHOT
     */
    private String extractShortName(String fullName) {
        if (fullName == null) {
            return "Unknown";
        }
        int index = fullName.indexOf('_');
        return index > 0 ? fullName.substring(index + 1) : fullName;
    }


    /**
     * 发送完成消息.
     *
     * @param context context
     * @param message message
     * @since 1.0.0-SNAPSHOT
     */
    private void sendCompleted(AgentContext context, String message) {
        try {
            ContentMessage msg = ContentMessage.builder()
                    .type(io.github.hijun.agent.common.enums.SseMessageType.COMPLETED)
                    .content(message)
                    .build();
            context.getSseEmitter().send(msg);
        } catch (IOException e) {
            log.error("发送完成消息失败", e);
        }
    }

    /**
     * 发送错误消息.
     *
     * @param context context
     * @param error error
     * @since 1.0.0-SNAPSHOT
     */
    private void sendError(AgentContext context, String error) {
        try {
            ContentMessage message = ContentMessage.builder()
                    .type(io.github.hijun.agent.common.enums.SseMessageType.ERROR)
                    .content(error)
                    .build();
            context.getSseEmitter().send(message);
        } catch (IOException e) {
            log.error("发送错误消息失败", e);
        }
    }
}
