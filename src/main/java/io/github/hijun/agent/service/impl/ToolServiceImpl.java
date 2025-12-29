package io.github.hijun.agent.service.impl;

import io.github.hijun.agent.service.ToolService;
import io.github.hijun.agent.tools.ToolDefinition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Tool Service Impl
 *
 * @author haijun
 * @email "mailto:haijun@email.com"
 * @date 2025/12/24 16:56
 * @version 3.4.3
 * @since 3.4.3
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToolServiceImpl implements ToolService {

    /**
     * tools.
     */
    private final List<ToolDefinition> tools;

    /**
     * Get All Tools
     *
     * @return list
     * @since 3.4.3
     */
    @Override
    public List<ToolDefinition> getAllTools() {
        return this.tools;
    }

    /**
     * Get Tool By Name
     *
     * @param name name
     * @return optional
     * @since 3.4.3
     */
    @Override
    public Optional<ToolDefinition> getToolByName(String name) {
        return this.tools.stream()
                .filter(tool -> tool.getName().equals(name))
                .findFirst();
    }

    /**
     * Get All Tool Callbacks
     *
     * @return list
     * @since 3.4.3
     */
    @Override
    public List<ToolCallback> getAllToolCallbacks() {
        return this.tools.stream()
                .map(this::convertToToolCallback)
                .collect(Collectors.toList());
    }

    /**
     * 将工具定义转换为Spring AI的ToolCallback
     *
     * @param tool 工具定义
     * @return ToolCallback
     * @since 3.4.3
     */
    private ToolCallback convertToToolCallback(ToolDefinition tool) {
        log.info("注册工具：{} - {}", tool.getName(), tool.getDescription());
        // 使用ParameterizedTypeReference指定输入类型为Map<String, Object>
        return FunctionToolCallback.builder(tool.getName(), tool::execute)
                .description(tool.getDescription())
                .inputType(new ParameterizedTypeReference<Map<String, Object>>() {})
                .build();
    }
}
