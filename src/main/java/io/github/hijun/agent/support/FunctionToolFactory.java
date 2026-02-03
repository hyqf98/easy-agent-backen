package io.github.hijun.agent.support;

import io.github.hijun.agent.service.McpConfigService;
import io.github.hijun.agent.tools.OrderTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 统一函数工具工厂
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/2/2 17:28
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FunctionToolFactory {

    /**
     * mcp config service.
     */
    private final McpConfigService mcpConfigService;

    /**
     * Get Tool Callbacks By Ids
     *
     * @param ids ids
     * @return tool callbacks
     * @since 1.0.0-SNAPSHOT
     */
    public List<ToolCallback> getToolCallbacksByIds(List<Long> ids) {
        return this.mcpConfigService.getToolCallbacksByIds(ids);
    }

    /**
     * Get Builtin Tools
     *
     * @return tool callbacks
     * @since 1.0.0-SNAPSHOT
     */
    public List<ToolCallback> getBuiltinTools() {
        return Arrays.asList(ToolCallbacks.from(new OrderTools()));
    }

    /**
     * Get All Tools
     *
     * @param mcpServerIds mcp server ids
     * @return tool callbacks
     * @since 1.0.0-SNAPSHOT
     */
    public List<ToolCallback> getAllTools(List<Long> mcpServerIds) {
        List<ToolCallback> builtinTools = this.getBuiltinTools();
        List<ToolCallback> mcpTools = this.getToolCallbacksByIds(mcpServerIds);

        List<ToolCallback> allTools = new ArrayList<>();
        allTools.addAll(builtinTools);
        allTools.addAll(mcpTools);

        return allTools;
    }
}
