package io.github.hijun.agent.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Tool Call Start Content
 *
 * @author haijun
 * @email "mailto:haijun@email.com"
 * @date 2025/12/24 16:53
 * @version 3.4.3
 * @since 3.4.3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCallStartContent {
    /**
     * 工具调用ID
     */
    private String toolCallId;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 工具描述
     */
    private String toolDescription;

    /**
     * 工具参数
     */
    private Map<String, Object> arguments;
}
