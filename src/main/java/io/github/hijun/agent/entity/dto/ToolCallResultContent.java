package io.github.hijun.agent.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tool Call Result Content
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
public class ToolCallResultContent {
    /**
     * 工具调用ID，关联开始消息
     */
    private String toolCallId;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 执行状态
     */
    private String status;

    /**
     * 结果内容
     */
    private Object result;

    /**
     * 结果类型（json/text/table）
     */
    private String resultType;

    /**
     * 执行耗时（毫秒）
     */
    private Long duration;
}
