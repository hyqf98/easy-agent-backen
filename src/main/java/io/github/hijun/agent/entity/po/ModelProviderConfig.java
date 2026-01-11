package io.github.hijun.agent.entity.po;

import com.baomidou.mybatisplus.annotation.IdGeneratorAssignt;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.hijun.agent.common.enums.ModelProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型提供商配置实体
 * <p>
 * 对应数据库表 model_provider_config，用于存储大模型提供商的配置信息
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
@TableName("model_provider_config")
public class ModelProviderConfig {

    /**
     * 主键ID
     * <p>
     * 使用雪花算法自动生成
     */
    @TableId(value = "id", type = IdGeneratorAssignt.ASSIGN_ID)
    private String id;

    /**
     * 提供商类型
     * <p>
     * 枚举值，表示具体的模型提供商（如 OpenAI、百度、阿里等）
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
     */
    @TableField("base_url")
    private String baseUrl;

    /**
     * 描述信息
     * <p>
     * 该配置的详细描述说明
     */
    @TableField("description")
    private String description;

    /**
     * 创建时间
     * <p>
     * 记录创建该配置的时间戳（毫秒）
     */
    @TableField("create_time")
    private Long createTime;

    /**
     * 更新时间
     * <p>
     * 记录最后更新该配置的时间戳（毫秒）
     */
    @TableField("update_time")
    private Long updateTime;
}
