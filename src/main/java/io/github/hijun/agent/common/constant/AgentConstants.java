package io.github.hijun.agent.common.constant;

/**
 * Agent常量定义
 *
 * @author haijun
 * @date 2025-12-24
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
public interface AgentConstants {


    /**
     * Agent Order
     *
     * @author haijun
     * @email "mailto:iamxiaohaijun@gmail.com"
     * @date 2026/1/13 15:32
     * @version 1.0.0-SNAPSHOT
     * @since 1.0.0-SNAPSHOT
     * @deprecated 使用 {@link AgentChain} 代替
     */
    @Deprecated
    class AgentOrder {

        /**
         * r e p o r t.
         */
        public static final String REPORT = "10000";

        /**
         * data collect.
         */
        public static final String DATA_COLLECT = "10001";
    }

    /**
     * 智能体 ID 常量。
     *
     * <p>定义各智能体的数字 ID 标识。</p>
     */
    class AgentIds {

        /**
         * PlanningAgent ID - 规划智能体。
         */
        public static final String PLANNING = "20001";

        /**
         * DataCollectAgent ID - 数据采集智能体。
         */
        public static final String DATA_COLLECT = "20002";

        /**
         * ContentGenAgent ID - 内容生成智能体。
         */
        public static final String CONTENT_GEN = "20003";
    }

    /**
     * 智能体名称常量。
     *
     * <p>定义各智能体的类名标识，用于构建完整的智能体键。</p>
     */
    class AgentNames {

        /**
         * PlanningAgent 名称。
         */
        public static final String PLANNING = "PlanningAgent";

        /**
         * DataCollectAgent 名称。
         */
        public static final String DATA_COLLECT = "DataCollectAgent";

        /**
         * ContentGenAgent 名称。
         */
        public static final String CONTENT_GEN = "ContentGenAgent";
    }

    /**
     * 智能体完整键名常量。
     *
     * <p>格式：{AgentId}_{AgentName}，用于智能体注册和查找。</p>
     */
    class AgentKeys {

        /**
         * PlanningAgent 完整键。
         */
        public static final String PLANNING_FULL = "20001_PlanningAgent";

        /**
         * DataCollectAgent 完整键。
         */
        public static final String DATA_COLLECT_FULL = "20002_DataCollectAgent";

        /**
         * ContentGenAgent 完整键。
         */
        public static final String CONTENT_GEN_FULL = "20003_ContentGenAgent";
    }

    /**
     * 智能体执行顺序常量。
     *
     * <p>定义报告生成流程中智能体的执行顺序。</p>
     */
    class AgentChain {

        /**
         * 报告生成智能体链。
         *
         * <p>执行顺序：PlanningAgent -> DataCollectAgent -> ContentGenAgent</p>
         */
        public static final java.util.List<String> REPORT_CHAIN = java.util.List.of(
            AgentKeys.PLANNING_FULL,
            AgentKeys.DATA_COLLECT_FULL,
            AgentKeys.CONTENT_GEN_FULL
        );
    }
}
