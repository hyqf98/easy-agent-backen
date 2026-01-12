package io.github.hijun.agent.entity.po;

import com.baomidou.mybatisplus.annotation.IdGeneratorAssignt;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型配置实体
 * <p>
 * 对应数据库表 model_config，用于存储具体模型的配置信息
 * 与模型提供商配置是多对一关系
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
@TableName("model_config")
public class ModelConfig {

    /**
     * 主键ID
     * <p>
     * 使用雪花算法自动生成
     */
    @TableId(value = "id", type = IdGeneratorAssignt.ASSIGN_ID)
    private String id;

    /**
     * 提供商配置ID
     * <p>
     * 关联 model_provider_config 表的主键
     */
    @TableField("provider_config_id")
    private String providerConfigId;

    /**
     * 模型ID
     * <p>
     * 调用 API 时使用的模型标识，如 gpt-4、claude-3-opus-20240229 等
     */
    @TableField("model_id")
    private String modelId;

    /**
     * 模型名称
     * <p>
     * 显示给用户的友好名称
     */
    @TableField("model_name")
    private String modelName;

    /**
     * 是否启用
     * <p>
     * true 表示启用该模型，false 表示禁用
     */
    @TableField("enabled")
    private Boolean enabled;

    /**
     * 描述信息
     * <p>
     * 该模型的详细描述说明
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
