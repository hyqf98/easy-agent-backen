package io.github.hijun.agent.entity.req;

import io.github.hijun.agent.common.enums.ModelProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 测试模型连接请求
 * <p>
 * 用于测试模型提供商的连接是否可用
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:haijun@email.com"
 * @date 2026/01/11
 * @since 3.4.3
 */
@Data
public class TestModelRequest {

    /**
     * 提供商配置ID
     * <p>
     * 模型提供商配置的唯一标识符
     */
    @NotBlank(message = "提供商ID不能为空")
    private String providerId;

    /**
     * 模型ID
     * <p>
     * 要测试的模型标识，如 gpt-4、claude-3-opus-20240229 等
     */
    @NotBlank(message = "模型ID不能为空")
    private String modelId;

    /**
     * 提供商类型
     * <p>
     * 枚举值，表示具体的模型提供商
     */
    @NotNull(message = "提供商类型不能为空")
    private ModelProvider providerType;

    /**
     * API Key
     * <p>
     * 用于测试连接的 API 密钥
     */
    private String apiKey;

    /**
     * Base URL
     * <p>
     * 模型提供商的 API 基础地址，可选
     */
    private String baseUrl;
}
