package io.github.hijun.agent.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.hijun.agent.common.enums.ModelProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 模型提供商配置实体
 * <p>
 * 对应数据库表 model_provider_config，用于存储大模型提供商的配置信息
 * 支持多种AI模型提供商：OpenAI、Anthropic、Ollama、Azure OpenAI等
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/01/11
 * @since 3.4.3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("model_provider_config")
public class ModelProviderConfig extends BasePo {

    /**
     * 提供商类型
     * <p>
     * 枚举值，表示具体的模型提供商（如 OPENAI、ANTHROPIC、OLLAMA、AZURE_OPENAI等）
     */
    @TableField("provider_type")
    private ModelProvider providerType;

    /**
     * 是否启用
     * <p>
     * true 表示启用该提供商，false 表示禁用
     */
    @TableField("enabled")
    private Boolean enabled;

    /**
     * API Key
     * <p>
     * 用于访问模型提供商 API 的密钥
     */
    @TableField("api_key")
    private String apiKey;

    /**
     * Base URL
     * <p>
     * 模型提供商的 API 基础地址，可自定义（如使用代理地址）
     * 对于Ollama等本地部署的模型，需要配置此地址
     */
    @TableField("base_url")
    private String baseUrl;

    /**
     * Azure资源名称
     * <p>
     * 仅用于Azure OpenAI，指定Azure OpenAI服务的资源名称
     */
    @TableField("azure_resource_name")
    private String azureResourceName;

    /**
     * Azure部署名称
     * <p>
     * 仅用于Azure OpenAI，指定具体的部署名称
     */
    @TableField("azure_deployment_name")
    private String azureDeploymentName;

    /**
     * 模型名称
     * <p>
     * 指定要使用的模型名称，如 gpt-4、claude-3-5-sonnet等
     */
    @TableField("model_name")
    private String modelName;

    /**
     * 温度参数
     * <p>
     * 控制模型输出的随机性，范围0-2，默认1.0
     */
    @TableField("temperature")
    private Double temperature;

    /**
     * 最大Token数
     * <p>
     * 限制单次请求的最大Token数量
     */
    @TableField("max_tokens")
    private Integer maxTokens;

    /**
     * 描述信息
     * <p>
     * 该配置的详细描述说明
     */
    @TableField("description")
    private String description;

    /**
     * 获取默认温度参数
     *
     * @return double
     * @since 1.0.0-SNAPSHOT
     */
    public Double getTemperature() {
        return this.temperature != null ? this.temperature : 0.7;
    }

    /**
     * 获取默认最大Token数
     *
     * @return integer
     * @since 1.0.0-SNAPSHOT
     */
    public Integer getMaxTokens() {
        return this.maxTokens != null ? this.maxTokens : 2048;
    }
}

