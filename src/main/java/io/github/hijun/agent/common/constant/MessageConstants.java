package io.github.hijun.agent.common.constant;

/**
 * 消息相关常量定义。
 *
 * <p>定义 SSE（Server-Sent Events）流式响应中使用的消息类型常量。</p>
 *
 * @author haijun
 * @date 2025-01-23
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
public interface MessageConstants {

    /**
     * SSE 消息类型常量。
     *
     * <p>前端根据此类型来区分不同的消息并采取相应的显示逻辑。</p>
     */
    class SseType {

        /**
         * 思考过程消息。
         *
         * <p>用于显示 AI 的推理过程，通常默认折叠显示。</p>
         */
        public static final String THINKING = "thinking";

        /**
         * 工具调用开始消息。
         *
         * <p>表示智能体开始调用某个工具，包含工具名称和参数。</p>
         */
        public static final String TOOL_CALL_START = "tool_call_start";

        /**
         * 工具调用结果消息。
         *
         * <p>包含工具执行的返回结果，默认展开显示。</p>
         */
        public static final String TOOL_CALL_RESULT = "tool_call_result";

        /**
         * 内容块消息。
         *
         * <p>流式传输生成的 Markdown 内容片段。</p>
         */
        public static final String CONTENT_CHUNK = "content_chunk";

        /**
         * 规划结果消息。
         *
         * <p>PlanningAgent 完成后发送，包含生成的计划内容。</p>
         */
        public static final String PLAN_RESULT = "plan_result";

        /**
         * 完成消息。
         *
         * <p>表示整个智能体链执行完成，包含最终结果的元数据。</p>
         */
        public static final String COMPLETED = "completed";

        /**
         * 错误消息。
         *
         * <p>包含执行过程中的错误信息。</p>
         */
        public static final String ERROR = "error";
    }
}
