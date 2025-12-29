package io.github.hijun.agent.tools;

import java.util.Map;

/**
 * 工具定义接口
 *
 * @author haijun
 * @date 2025-12-24
 */
public interface ToolDefinition {
    /**
     * 获取工具名称
     *
     * @return 工具名称
     */
    String getName();

    /**
     * 获取工具描述
     *
     * @return 工具描述
     */
    String getDescription();

    /**
     * 执行工具
     *
     * @param arguments 参数
     * @return 执行结果
     */
    Object execute(Map<String, Object> arguments);

    /**
     * 获取工具参数定义（JSON Schema格式）
     *
     * @return 参数定义
     */
    Map<String, Object> getParametersSchema();
}
