package io.github.hijun.agent.entity.req;

import io.github.hijun.agent.common.enums.ModelProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 大语言模型配置查询条件
 * <p>
 * 用于查询模型配置列表的请求参数
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
@Schema(description = "大语言模型配置查询条件")
public class LlmModelQuery extends BaseQuery {

    /**
     * 提供商类型
     * <p>
     * 按提供商筛选：openai、zhipuai、anthropic
     */
    @Schema(description = "提供商类型")
    private ModelProvider providerType;

    /**
     * 是否启用
     * <p>
     * 按启用状态筛选
     */
    @Schema(description = "是否启用")
    private Boolean enabled;

    /**
     * 是否支持工具调用
     * <p>
     * 按工具调用能力筛选
     */
    @Schema(description = "是否支持工具调用")
    private Boolean supportTools;

    /**
     * 是否支持视觉识别
     * <p>
     * 按视觉识别能力筛选
     */
    @Schema(description = "是否支持视觉识别")
    private Boolean supportVision;

    /**
     * 关键词搜索
     * <p>
     * 模糊匹配模型编码或名称
     */
    @Schema(description = "关键词搜索（模糊匹配模型编码或名称）")
    private String keyword;
}
