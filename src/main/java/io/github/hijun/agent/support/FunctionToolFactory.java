package io.github.hijun.agent.support;

import io.github.hijun.agent.service.McpConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
        // 内置工具暂时为空，FileTools 的 @Tool 注解方式需要通过不同的方式获取
        // 可以考虑使用 Spring AI 的 ToolReference 或者自定义注册方式
        return List.of();
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
