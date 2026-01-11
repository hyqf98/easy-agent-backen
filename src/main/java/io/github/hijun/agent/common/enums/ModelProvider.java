package io.github.hijun.agent.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 模型提供商枚举
 * <p>
 * 定义系统支持的所有大模型提供商及其配置信息
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:haijun@email.com"
 * @date 2026/01/11
 * @since 3.4.3
 */
@Getter
@AllArgsConstructor
public enum ModelProvider {

    /**
     * OpenAI 提供商
     * <p>
     * 支持 GPT-4、GPT-3.5 等模型
     */
    OPENAI("openai", "OpenAI", true, true, new String[]{"gpt-4", "gpt-4-turbo", "gpt-3.5-turbo"}),

    /**
     * Anthropic 提供商
     * <p>
     * 支持 Claude 3 Opus/Sonnet/Haiku 等模型
     */
    ANTHROPIC("anthropic", "Anthropic", true, false, new String[]{"claude-3-opus-20240229", "claude-3-sonnet-20240229", "claude-3-haiku-20240307"}),

    /**
     * Google 提供商
     * <p>
     * 支持 Gemini Pro 等模型
     */
    GOOGLE("google", "Google", true, true, new String[]{"gemini-pro", "gemini-pro-vision"}),

    /**
     * 百度 提供商
     * <p>
     * 支持文心一言 ERNIE-Bot 等模型
     */
    BAIDU("baidu", "百度", true, false, new String[]{"ernie-bot-4", "ernie-bot-turbo"}),

    /**
     * 阿里 提供商
     * <p>
     * 支持通义千问 Qwen 等模型
     */
    ALIBABA("alibaba", "阿里", true, false, new String[]{"qwen-max", "qwen-plus", "qwen-turbo"}),

    /**
     * 智谱 提供商
     * <p>
     * 支持 ChatGLM 等模型
     */
    ZHIPU("zhipu", "智谱", true, false, new String[]{"glm-4", "glm-3-turbo"}),

    /**
     * Moonshot 提供商
     * <p>
     * 支持 Moonshot-v1 等模型
     */
    MOONSHOT("moonshot", "Moonshot", true, true, new String[]{"moonshot-v1-128k", "moonshot-v1-32k"}),

    /**
     * DeepSeek 提供商
     * <p>
     * 支持 DeepSeek-Chat、DeepSeek-Coder 等模型
     */
    DEEPSEEK("deepseek", "DeepSeek", true, true, new String[]{"deepseek-chat", "deepseek-coder"}),

    /**
     * 字节 提供商
     * <p>
     * 支持豆包 Doubao 等模型
     */
    BYTEDANCE("bytedance", "字节", true, false, new String[]{"doubao-pro", "doubao-lite"});

    /**
     * 提供商代码
     * <p>
     * 用于唯一标识提供商，通常是小写字母
     */
    private final String code;

    /**
     * 提供商名称
     * <p>
     * 显示给用户的友好名称
     */
    private final String name;

    /**
     * 是否需要 API Key
     * <p>
     * true 表示必须配置 API Key 才能使用
     */
    private final boolean requireApiKey;

    /**
     * 是否需要 Base URL
     * <p>
     * true 表示用户可以自定义 Base URL（如使用代理）
     */
    private final boolean requireBaseUrl;

    /**
     * 默认模型列表
     * <p>
     * 该提供商支持的默认模型 ID 数组
     */
    private final String[] defaultModels;

    /**
     * 根据代码获取枚举实例
     * <p>
     * 通过提供商代码查找对应的枚举常量
     *
     * @param code 提供商代码
     * @return 对应的枚举实例
     * @throws IllegalArgumentException 如果代码不匹配任何提供商
     */
    public static ModelProvider fromCode(String code) {
        for (ModelProvider provider : values()) {
            if (provider.code.equals(code)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown model provider: " + code);
    }
}
