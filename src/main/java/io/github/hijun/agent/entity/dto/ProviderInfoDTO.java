package io.github.hijun.agent.entity.dto;

import io.github.hijun.agent.common.enums.ModelProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 模型提供商信息 DTO
 * <p>
 * 用于返回系统支持的模型提供商信息
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:haijun@email.com"
 * @date 2026/01/11
 * @since 3.4.3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderInfoDTO {

    /**
     * 提供商类型
     * <p>
     * 枚举值，表示具体的模型提供商
     */
    private ModelProvider providerType;

    /**
     * 提供商名称
     * <p>
     * 显示给用户的友好名称
     */
    private String name;

    /**
     * 是否需要 API Key
     * <p>
     * true 表示必须配置 API Key 才能使用
     */
    private Boolean requireApiKey;

    /**
     * 是否需要 Base URL
     * <p>
     * true 表示用户可以自定义 Base URL
     */
    private Boolean requireBaseUrl;

    /**
     * 默认模型列表
     * <p>
     * 该提供商支持的默认模型 ID 列表
     */
    private List<String> defaultModels;
}
