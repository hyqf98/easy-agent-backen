package io.github.hijun.agent.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Report Update Content
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
public class ReportUpdateContent {
    /**
     * 报告完整内容
     */
    private String content;

    /**
     * 报告格式（markdown/html）
     */
    @Builder.Default
    private String format = "markdown";

    /**
     * 更新进度（0-100）
     */
    private Integer progress;
}
