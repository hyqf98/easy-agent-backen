package io.github.hijun.agent.service;

import io.github.hijun.agent.tools.ToolDefinition;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.Optional;

/**
 * Tool Service
 *
 * @author haijun
 * @email "mailto:haijun@email.com"
 * @date 2025/12/24 16:56
 * @version 3.4.3
 * @since 3.4.3
 */
public interface ToolService {
    /**
     * 获取所有工具
     *
     * @return 工具列表
     * @since 3.4.3
     */
    List<ToolDefinition> getAllTools();

    /**
     * 根据名称获取工具
     *
     * @param name 工具名称
     * @return 工具定义
     * @since 3.4.3
     */
    Optional<ToolDefinition> getToolByName(String name);

    /**
     * 获取所有Tool Callback（用于Spring AI）
     *
     * @return Tool Callback列表
     * @since 3.4.3
     */
    List<ToolCallback> getAllToolCallbacks();
}
