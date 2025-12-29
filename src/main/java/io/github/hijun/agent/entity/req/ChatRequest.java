package io.github.hijun.agent.entity.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Chat Request
 *
 * @author haijun
 * @email "mailto:haijun@email.com"
 * @date 2025/12/24 16:53
 * @version 3.4.3
 * @since 3.4.3
 */
@Data
public class ChatRequest {
    /**
     * 用户消息
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /**
     * 聊天模式
     */
    private String mode = "chat";

    /**
     * 会话ID（可选，用于多轮对话）
     */
    private String sessionId;
}
