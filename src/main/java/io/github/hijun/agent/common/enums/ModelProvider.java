package io.github.hijun.agent.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 模型提供商枚举
 * <p>
 * 定义支持的AI模型提供商
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Getter
@AllArgsConstructor
public enum ModelProvider implements BaseEnum<String> {

    /**
     * OpenAI
     */
    OPENAI("openai", "OpenAI"),

    /**
     * 智谱AI (Zhipu AI)
     */
    ZHIPU_AI("zhipuai", "智谱AI"),

    /**
     * Anthropic
     */
    ANTHROPIC("anthropic", "Anthropic");

    /**
     * 模型提供商代码
     */
    private final String value;

    /**
     * 模型提供商描述
     */
    private final String desc;
}
