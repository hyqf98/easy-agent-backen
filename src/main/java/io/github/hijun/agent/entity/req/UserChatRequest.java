package io.github.hijun.agent.entity.req;

import io.github.hijun.agent.common.enums.ModelProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天请求表单
 * <p>
 * 用于接收前端发送的聊天消息请求
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "聊天请求表单")
public class UserChatRequest {

    /**
     * 用户消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    @Schema(description = "用户消息内容")
    private String message;

    /**
     * 会话ID（可选，用于多轮对话）
     */
    @Schema(description = "会话ID", example = "session-123")
    private String sessionId;

    /**
     * 模型提供商（可选，默认使用配置的默认提供商）
     * 支持的值: openai, zhipuai, anthropic
     */
    @Schema(description = "模型提供商", example = "openai", allowableValues = {"openai", "zhipuai", "anthropic"})
    @NotNull(message = "模型供应商不能为空")
    private ModelProvider provider;

    /**
     * 具体模型名称（可选，覆盖配置中的默认模型）
     * 例如: gpt-4o, glm-4-air, claude-sonnet-4-5
     */
    @Size(max = 100, message = "模型名称长度不能超过100")
    @Schema(description = "具体模型名称", example = "gpt-4o")
    private String model;
}
