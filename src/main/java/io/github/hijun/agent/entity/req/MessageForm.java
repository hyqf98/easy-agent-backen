package io.github.hijun.agent.entity.req;

import io.github.hijun.agent.common.enums.MessageRole;
import io.github.hijun.agent.common.enums.MessageStatus;
import org.springframework.ai.chat.messages.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 消息表单
 * <p>
 * 用于新增和修改消息的请求表单
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "消息表单")
public class MessageForm extends BaseForm {

    /**
     * 会话ID
     */
    @NotNull(message = "会话ID不能为空")
    @Schema(description = "会话ID")
    private Long sessionId;

    /**
     * 角色
     */
    @NotNull(message = "角色不能为空")
    @Schema(description = "角色")
    private MessageRole role;

    /**
     * 消息类型
     */
    @NotNull(message = "消息类型不能为空")
    @Schema(description = "消息类型")
    private MessageType type;

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    @Schema(description = "消息内容")
    private String content;

    /**
     * 消息状态
     */
    @Schema(description = "消息状态")
    private MessageStatus status;
}
