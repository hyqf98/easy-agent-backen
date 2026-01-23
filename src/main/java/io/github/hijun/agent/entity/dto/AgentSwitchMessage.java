package io.github.hijun.agent.entity.dto;

import io.github.hijun.agent.common.enums.SseMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 智能体切换消息.
 * <p>通知前端智能体正在切换</p>
 *
 * @author hijun
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AgentSwitchMessage extends SseMessage {

    /**
     * 当前智能体名称.
     */
    private String fromAgent;

    /**
     * 下一个智能体名称.
     */
    private String toAgent;

    /**
     * 切换原因.
     */
    private String reason;

    /**
     * 智能体 ID.
     */
    private String agentId;
}
