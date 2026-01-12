package io.github.hijun.agent.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型信息 DTO
 * <p>
 * 用于返回单个模型的信息
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
public class ModelInfoDTO {

    /**
     * 模型配置ID
     * <p>
     * 模型配置的唯一标识符
     */
    private String id;

    /**
     * 模型名称
     * <p>
     * 显示给用户的友好名称
     */
    private String name;

    /**
     * 是否启用
     * <p>
     * true 表示启用该模型，false 表示禁用
     */
    private Boolean enabled;
}
