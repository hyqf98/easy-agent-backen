package io.github.hijun.agent.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.hijun.agent.common.enums.ModelProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 大语言模型配置
 * <p>
 * 存储所有模型的配置信息，包括 OpenAI、智谱AI、Anthropic 等提供商的模型
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
@TableName("llm_model")
public class LlmModel extends BasePo implements Serializable {

    /**
     * 模型编码（唯一标识）
     * <p>
     * 如：gpt-4o、glm-4-air、claude-sonnet-4-5
     */
    @TableField("model_code")
    private String modelCode;

    /**
     * 模型显示名称
     * <p>
     * 如：GPT-4o、GLM-4-Air、Claude Sonnet 4.5
     */
    @TableField("model_name")
    private String modelName;

    /**
     * 提供商类型
     * <p>
     * openai、zhipuai、anthropic
     */
    @TableField("provider_type")
    private ModelProvider providerType;

    /**
     * API密钥
     */
    @TableField("api_key")
    private String apiKey;

    /**
     * API基础URL
     * <p>
     * 可选，用于代理或自定义端点
     */
    @TableField("base_url")
    private String baseUrl;

    /**
     * 温度参数
     * <p>
     * 控制输出的随机性，范围 0.0-2.0
     */
    @TableField("temperature")
    private Double temperature;

    /**
     * 最大生成Token数
     */
    @TableField("max_tokens")
    private Integer maxTokens;

    /**
     * 最大上下文窗口Token数
     * <p>
     * 如：GPT-4o 是 128000，Claude Sonnet 4.5 是 200000
     */
    @TableField("max_context_window")
    private Integer maxContextWindow;

    /**
     * Top-P采样
     * <p>
     * 核采样参数，范围 0.0-1.0
     */
    @TableField("top_p")
    private Double topP;

    /**
     * Top-K采样
     * <p>
     * 部分厂商支持
     */
    @TableField("top_k")
    private Integer topK;

    /**
     * 是否支持工具调用
     * <p>
     * 如 Function Calling
     */
    @TableField("support_tools")
    private Boolean supportTools;

    /**
     * 是否支持视觉识别
     * <p>
     * 如图片理解能力
     */
    @TableField("support_vision")
    private Boolean supportVision;

    /**
     * 扩展参数
     * <p>
     * JSON格式，存储厂商特定参数
     */
    @TableField("extra_params")
    private String extraParams;

    /**
     * 是否启用
     * <p>
     * 0-禁用，1-启用
     */
    @TableField("enabled")
    private Boolean enabled;

    /**
     * 是否为默认模型
     * <p>
     * 0-否，1-是
     */
    @TableField("is_default")
    private Boolean isDefault;

    /**
     * 排序号
     * <p>
     * 用于列表展示排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}
