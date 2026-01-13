package io.github.hijun.agent.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

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
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ModelInfoDTO extends BaseDTO {


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
