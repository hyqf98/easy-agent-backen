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
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/01/11
 * @since 3.4.3
 */
@Getter
@AllArgsConstructor
public enum ModelProvider {

    /**
     * Anthropic 提供商
     * <p>
     * 支持 Claude 3 Opus/Sonnet/Haiku 等模型
     */
    ANTHROPIC("anthropic", "Anthropic", true, false, new String[]{"claude-3-5-sonnet-20241022", "claude-3-opus-20240229", "claude-3-sonnet-20240229", "claude-3-haiku-20240307"}),

    /**
     * Azure OpenAI 提供商
     * <p>
     * 微软Azure托管的OpenAI服务
     */
    AZURE_OPENAI("azure_openai", "Azure OpenAI", true, true, new String[]{"gpt-4", "gpt-35-turbo"}),

    /**
     * HuggingFace 提供商
     * <p>
     * 使用 HuggingFace 的推理 API（兼容 OpenAI API）
     */
    HUGGINGFACE("huggingface", "HuggingFace", true, true, new String[]{"meta-llama/Llama-3.2-3B-Instruct", "mistralai/Mistral-7B-Instruct-v0.2"}),

    /**
     * MiniMax 提供商
     * <p>
     * 支持 MiniMax 系列 AI 模型
     */
    MINIMAX("minimax", "MiniMax", true, true, new String[]{"abab6.5s-chat", "abab5.5-chat"}),

    /**
     * Moonshot AI 提供商
     * <p>
     * 支持 Moonshot-v1 等模型
     */
    MOONSHOT("moonshot", "Moonshot AI", true, true, new String[]{"moonshot-v1-128k", "moonshot-v1-32k", "moonshot-v1-8k"}),

    /**
     * Ollama 提供商
     * <p>
     * 本地部署的开源模型，支持 Llama、Mistral 等
     */
    OLLAMA("ollama", "Ollama", false, true, new String[]{"llama3.2", "mistral", "qwen2", "gemma2"}),

    /**
     * OpenAI 提供商
     * <p>
     * 支持 GPT-4、GPT-3.5 等模型
     */
    OPENAI("openai", "OpenAI", true, true, new String[]{"gpt-4o", "gpt-4o-mini", "gpt-4-turbo", "gpt-3.5-turbo"}),

    /**
     * ZhiPu AI 提供商
     * <p>
     * 支持 ChatGLM 等模型
     */
    ZHIPU("zhipu", "ZhiPu AI", true, true, new String[]{"glm-4", "glm-4-plus", "glm-4-flash", "glm-3-turbo"});

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
     * @since 1.0.0-SNAPSHOT
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
