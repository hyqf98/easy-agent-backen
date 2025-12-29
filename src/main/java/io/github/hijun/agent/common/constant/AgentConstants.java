package io.github.hijun.agent.common.constant;

/**
 * Agent常量定义
 *
 * @author haijun
 * @date 2025-12-24
 */
public interface AgentConstants {
    /**
     * SSE超时时间（毫秒）
     */
    Long SSE_TIMEOUT = 5 * 60 * 1000L;

    /**
     * SSE心跳间隔（毫秒）
     */
    Long SSE_HEARTBEAT_INTERVAL = 10 * 1000L;

    /**
     * 最大工具调用次数
     */
    Integer MAX_TOOL_CALLS = 10;

    /**
     * 系统提示词 - 聊天模式（ReAct格式）
     */
    String SYSTEM_PROMPT_CHAT = """
            你是一个基于ReAct（Reasoning and Acting）框架的智能助手。

            请使用以下格式来回答问题：

            Thought: 分析问题，思考需要采取什么行动
            Action: 如果需要使用工具，说明工具名称和参数；如果不需要工具，可以直接给出答案
            Observation: 工具执行的结果（由系统自动填充）
            ... (重复 Thought/Action/Observation 循环，直到有足够信息回答问题)
            Thought: 我现在知道最终答案了
            Final Answer: 给用户的最终回答

            可用工具：
            - webSearch: 搜索互联网信息
            - calculator: 执行数学计算
            - weather: 查询城市天气

            重要规则：
            1. 每次只能执行一个Action
            2. 必须先Thought再Action
            3. 如果不需要工具，可以直接给出Final Answer
            4. Final Answer必须是对用户友好的自然语言回答
            """;

    /**
     * 系统提示词 - 报告模式
     */
    String SYSTEM_PROMPT_REPORT = """
            你是一个专业的报告生成助手，擅长收集信息并生成结构化的Markdown报告。
            请按照以下步骤工作：
            1. 分析用户需求，确定需要收集的信息
            2. 使用合适的工具收集相关信息
            3. 整理信息并生成结构化的Markdown报告
            4. 报告应包含：标题、摘要、详细内容、结论等部分
            """;
}
