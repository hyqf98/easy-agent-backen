package io.github.hijun.agent.entity.dto;

import io.github.hijun.agent.common.enums.ModelProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 模型提供商配置 DTO
 * <p>
 * 用于返回单个模型提供商的配置信息
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/01/11
 * @since 3.4.3
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ModelProviderDTO extends BaseDTO {


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
    private String providerName;

    /**
     * 是否启用
     * <p>
     * true 表示启用该提供商，false 表示禁用
     */
    private Boolean enabled;

    /**
     * API Key（脱敏）
     * <p>
     * 经过脱敏处理的 API Key，只显示部分字符
     */
    private String apiKey;

    /**
     * Base URL
     * <p>
     * 模型提供商的 API 基础地址
     */
    private String baseUrl;

    /**
     * 模型列表
     * <p>
     * 该提供商下配置的所有模型
     */
    private List<ModelInfoDTO> models;
}
