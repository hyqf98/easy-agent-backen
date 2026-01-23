package io.github.hijun.agent.entity.dto;

import io.github.hijun.agent.common.enums.SseMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 规划结果消息.
 * <p>规划智能体输出的任务分解结果</p>
 *
 * @author hijun
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PlanResultMessage extends SseMessage {

    /**
     * 规划描述.
     */
    private String plan;

    /**
     * 执行步骤列表.
     */
    private List<String> steps;

    /**
     * 预计耗时（秒）.
     */
    private Integer estimatedTime;
}
