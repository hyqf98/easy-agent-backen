package io.github.hijun.agent.entity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 基础DTO类
 * <p>
 * 所有数据传输对象的基类，包含公共字段：id、创建时间、更新时间
 *
 * @author haijun
 * @version 3.4.3
 * @date 2026/01/12
 */
@Data
public abstract class BaseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private String id;

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
