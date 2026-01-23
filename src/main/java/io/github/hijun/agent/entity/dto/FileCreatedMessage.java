package io.github.hijun.agent.entity.dto;

import io.github.hijun.agent.common.enums.SseMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 文件创建消息.
 * <p>通知前端文件已创建</p>
 *
 * @author hijun
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FileCreatedMessage extends SseMessage {

    /**
     * 文件名.
     */
    private String fileName;

    /**
     * 完整路径.
     */
    private String filePath;

    /**
     * 文件类型.
     * <p>可选值: plan, data, content, final</p>
     */
    private String fileType;

    /**
     * 文件大小（字节）.
     */
    private Long fileSize;
}
