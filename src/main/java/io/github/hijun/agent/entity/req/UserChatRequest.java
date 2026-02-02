package io.github.hijun.agent.entity.req;

import io.github.hijun.agent.common.enums.AdditionalFeatures;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
     * 模型ID（必填，用于查询数据库获取模型配置）
     */
    @Schema(description = "模型ID", example = "1")
    @NotNull(message = "模型ID不能为空")
    private Long modelId;

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
     * 请求ID
     */
    @Schema(description = "请求ID")
    @NotBlank(message = "请求ID不能为空")
    private String requestId;

    /**
     * 文件列表
     */
    @Schema(description = "文件列表")
    private List<String> files;

    /**
     * additional features.
     */
    @Schema(description = "模型开启的功能")
    private List<AdditionalFeatures> additionalFeatures;

    /**
     * tool ids.
     */
    @Schema(description = "可用工具id")
    private List<Long> toolIds;
}
