package io.github.hijun.agent.entity.dto;

import io.github.hijun.agent.common.enums.MessageRole;
import io.github.hijun.agent.common.enums.MessageStatus;
import org.springframework.ai.chat.messages.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 消息DTO
 * <p>
 * 用于返回消息信息的响应对象
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "消息响应")
public class MessageDTO extends BaseDTO {

    /**
     * 会话ID
     */
    @Schema(description = "会话ID")
    private Long sessionId;

    /**
     * 角色
     */
    @Schema(description = "角色")
    private MessageRole role;

    /**
     * 消息类型
     */
    @Schema(description = "消息类型")
    private MessageType type;

    /**
     * 消息内容
     */
    @Schema(description = "消息内容")
    private String content;

    /**
     * 消息状态
     */
    @Schema(description = "消息状态")
    private MessageStatus status;
}
