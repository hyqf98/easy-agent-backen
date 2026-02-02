package io.github.hijun.agent.entity.req;

import io.github.hijun.agent.common.enums.ModelProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 大语言模型配置表单
 * <p>
 * 用于新增和修改模型配置的请求表单
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "大语言模型配置表单")
public class LlmModelForm extends BaseForm {

    /**
     * 模型编码
     * <p>
     * 唯一标识，如：gpt-4o、glm-4-air、claude-sonnet-4-5
     */
    @NotBlank(message = "模型编码不能为空")
    @Size(max = 100, message = "模型编码长度不能超过100")
    @Schema(description = "模型编码", example = "gpt-4o")
    private String modelCode;

    /**
     * 模型显示名称
     * <p>
     * 如：GPT-4o、GLM-4-Air、Claude Sonnet 4.5
     */
    @NotBlank(message = "模型名称不能为空")
    @Size(max = 100, message = "模型名称长度不能超过100")
    @Schema(description = "模型显示名称", example = "GPT-4o")
    private String modelName;

    /**
     * 提供商类型
     * <p>
     * openai、zhipuai、anthropic
     */
    @NotNull(message = "提供商类型不能为空")
    @Schema(description = "提供商类型", example = "openai")
    private ModelProvider providerType;

    /**
     * API密钥
     */
    @NotBlank(message = "API密钥不能为空")
    @Size(max = 500, message = "API密钥长度不能超过500")
    @Schema(description = "API密钥")
    private String apiKey;

    /**
     * API基础URL
     * <p>
     * 可选，用于代理或自定义端点
     */
    @Size(max = 500, message = "URL长度不能超过500")
    @Schema(description = "API基础URL")
    private String baseUrl;

    /**
     * 温度参数
     * <p>
     * 控制输出的随机性，范围 0.0-2.0
     */
    @Schema(description = "温度参数（0.0-2.0）", example = "0.7")
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
    @Schema(description = "Top-P采样（0.0-1.0）", example = "1.0")
    private Double topP;

    /**
     * Top-K采样
     * <p>
     * 部分厂商支持
     */
    @Schema(description = "Top-K采样")
    private Integer topK;

    /**
     * 是否支持工具调用
     * <p>
     * 如 Function Calling
     */
    @NotNull(message = "工具调用支持不能为空")
    @Schema(description = "是否支持工具调用", example = "true")
    private Boolean supportTools;

    /**
     * 是否支持视觉识别
     * <p>
     * 如图片理解能力
     */
    @NotNull(message = "视觉识别支持不能为空")
    @Schema(description = "是否支持视觉识别", example = "true")
    private Boolean supportVision;

    /**
     * 扩展参数
     * <p>
     * JSON格式，存储厂商特定参数
     */
    @Schema(description = "扩展参数（JSON格式）")
    private String extraParams;

    /**
     * 是否启用
     * <p>
     * 0-禁用，1-启用
     */
    @NotNull(message = "启用状态不能为空")
    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    /**
     * 是否为默认模型
     * <p>
     * 0-否，1-是
     */
    @NotNull(message = "默认模型标记不能为空")
    @Schema(description = "是否为默认模型", example = "false")
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
    @Size(max = 500, message = "备注长度不能超过500")
    @Schema(description = "备注")
    private String remark;
}
