package io.github.hijun.agent.entity.dto;

import io.github.hijun.agent.common.enums.ModelProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 大语言模型配置 DTO
 * <p>
 * 用于返回模型配置信息的响应对象
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "大语言模型配置响应")
public class LlmModelDTO extends BaseDTO {

    /**
     * 模型编码
     * <p>
     * 唯一标识，如：gpt-4o、glm-4-air、claude-sonnet-4-5
     */
    @Schema(description = "模型编码", example = "gpt-4o")
    private String modelCode;

    /**
     * 模型显示名称
     * <p>
     * 如：GPT-4o、GLM-4-Air、Claude Sonnet 4.5
     */
    @Schema(description = "模型显示名称", example = "GPT-4o")
    private String modelName;

    /**
     * 提供商类型
     * <p>
     * openai、zhipuai、anthropic
     */
    @Schema(description = "提供商类型", example = "openai")
    private ModelProvider providerType;

    /**
     * 提供商描述
     * <p>
     * 提供商的中文名称，如：OpenAI、智谱AI、Anthropic
     */
    @Schema(description = "提供商描述", example = "OpenAI")
    private String providerDesc;

    /**
     * API密钥
     */
    @Schema(description = "API密钥")
    private String apiKey;

    /**
     * API基础URL
     * <p>
     * 用于代理或自定义端点
     */
    @Schema(description = "API基础URL")
    private String baseUrl;

    /**
     * 温度参数
     * <p>
     * 控制输出的随机性，范围 0.0-2.0
     */
    @Schema(description = "温度参数", example = "0.7")
    private Double temperature;

    /**
     * 最大生成Token数
     */
    @Schema(description = "最大生成Token数", example = "2000")
    private Integer maxTokens;

    /**
     * 最大上下文窗口Token数
     * <p>
     * 如：GPT-4o 是 128000，Claude Sonnet 4.5 是 200000
     */
    @Schema(description = "最大上下文窗口Token数", example = "128000")
    private Integer maxContextWindow;

    /**
     * Top-P采样
     * <p>
     * 核采样参数，范围 0.0-1.0
     */
    @Schema(description = "Top-P采样", example = "1.0")
    private Double topP;

    /**
     * 是否支持工具调用
     * <p>
     * 如 Function Calling
     */
    @Schema(description = "是否支持工具调用", example = "true")
    private Boolean supportTools;

    /**
     * 是否支持视觉识别
     * <p>
     * 如图片理解能力
     */
    @Schema(description = "是否支持视觉识别", example = "true")
    private Boolean supportVision;

    /**
     * 是否启用
     * <p>
     * 0-禁用，1-启用
     */
    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    /**
     * 是否为默认模型
     * <p>
     * 0-否，1-是
     */
    @Schema(description = "是否为默认模型", example = "true")
    private Boolean isDefault;

    /**
     * 排序号
     * <p>
     * 用于列表展示排序
     */
    @Schema(description = "排序号", example = "0")
    private Integer sortOrder;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
}
