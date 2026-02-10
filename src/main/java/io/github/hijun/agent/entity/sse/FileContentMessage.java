package io.github.hijun.agent.entity.sse;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Builder;
import lombok.Data;

/**
 * File Info Message
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/2/9 14:22
 * @since 1.0.0-SNAPSHOT
 */
@Data
@Builder
@JsonClassDescription("文件消息实体")
public class FileInfoMessage {

    /**
     * name.
     */
    @JsonPropertyDescription("File name")
    private String name;

    /**
     * url.
     */
    @JsonPropertyDescription("File path")
    private String path;

    /**
     * description.
     */
    @JsonPropertyDescription("File description")
    private String description;
}
