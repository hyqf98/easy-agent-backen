package io.github.hijun.agent.entity.req;

import io.github.hijun.agent.common.enums.AdditionalFeatures;
import io.github.hijun.agent.common.enums.ChatMode;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

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
     * 自定义提示词
     */
    private String userPrompt;

    /**
     * 用户消息
     */
    @NotBlank(message = "消息内容不能为空")
    private String userQuery;

    /**
     * 会话ID（可选，用于多轮对话）
     */
    private String sessionId;

    /**
     * request id.
     */
    private String requestId;

    /**
     * 聊天模式
     */
    private ChatMode mode;

    /**
     * 附加功能
     */
    private List<AdditionalFeatures> additionalFeatures;

    /**
     * 用户提交的文件连接信息
     */
    private List<String> userUploadFiles;
}
