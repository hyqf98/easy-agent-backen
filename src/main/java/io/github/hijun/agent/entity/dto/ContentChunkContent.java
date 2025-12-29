package io.github.hijun.agent.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Content Chunk Content
 *
 * @author haijun
 * @email "mailto:haijun@email.com"
 * @date 2025/12/24 16:53
 * @version 3.4.3
 * @since 3.4.3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentChunkContent {
    /**
     * 内容片段
     */
    private String chunk;

    /**
     * 是否为Markdown格式
     */
    @Builder.Default
    private Boolean isMarkdown = true;
}
