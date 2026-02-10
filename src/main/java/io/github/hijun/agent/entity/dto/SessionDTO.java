package io.github.hijun.agent.entity.dto;

import io.github.hijun.agent.common.enums.ChatMode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

/**
 * 会话DTO
 * <p>
 * 用于返回会话信息的响应对象
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
@Schema(description = "会话响应")
public class SessionDTO extends BaseDTO {

    /**
     * 会话标题
     */
    @Schema(description = "会话标题")
    private String title;

    /**
     * 模型ID
     */
    @Schema(description = "模型ID")
    private Long modelId;

    /**
     * 聊天模式
     */
    @Schema(description = "聊天模式")
    private ChatMode chatMode;

    /**
     * 消息数量
     * <p>
     * 非数据库字段
     */
    @Schema(description = "消息数量")
    private Long messageCount;
}
