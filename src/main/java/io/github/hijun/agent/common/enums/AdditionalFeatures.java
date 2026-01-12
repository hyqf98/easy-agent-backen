package io.github.hijun.agent.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Additional Features
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/1/7 16:36
 * @since 1.0.0-SNAPSHOT
 */
@Getter
@AllArgsConstructor
public enum AdditionalFeatures {

    /**
     * deep thinking.
     */
    DEEP_THINKING("deep_thinking", "深度思考"),

    /**
     * deep search.
     */
    DEEP_SEARCH("deep_search", "深度搜索");

    /**
     * code.
     */
    private final String code;
    /**
     * description.
     */
    private final String description;
}
