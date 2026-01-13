package io.github.hijun.agent.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 基础DTO类
 * <p>
 * 所有数据传输对象的基类，包含公共字段：id、创建时间、更新时间
 *
 * @author haijun
 * @version 3.4.3
 * @date 2026/01/12
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @since 1.0.0-SNAPSHOT
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseDTO implements Serializable {

    /**
     * serial version u i d.
     */
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 创建时间
     * <p>
     * 记录创建该实体的时间戳（毫秒）
     */
    private Long createTime;

    /**
     * 更新时间
     * <p>
     * 记录最后更新该实体的时间戳（毫秒）
     */
    private Long updateTime;
}
