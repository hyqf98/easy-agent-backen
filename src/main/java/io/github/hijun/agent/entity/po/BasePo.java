package io.github.hijun.agent.entity.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Date;

/**
 * 基础实体类
 * <p>
 * 所有数据库实体类的基类，包含公共字段：id、创建时间、更新时间、逻辑删除标记
 *
 * @author haijun
 * @version 3.4.3
 * @date 2026/01/12
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @since 1.0.0-SNAPSHOT
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class BasePo implements Serializable {

    /**
     * serial version u i d.
     */
    private static final long serialVersionUID = 1L;

    /**
     * i d.
     */
    public static final String ID = "id";

    /**
     * create time.
     */
    public static final String CREATE_TIME = "create_time";

    /**
     * update time.
     */
    public static final String UPDATE_TIME = "update_time";

    /**
     * 主键ID
     * <p>
     * 使用数据库自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间
     * <p>
     * 记录创建该实体的时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     * <p>
     * 记录最后更新该实体的时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 逻辑删除标记
     * <p>
     * MyBatis Plus 会自动处理：
     * - 查询时自动添加 WHERE deleted = 0
     * - 删除时自动执行 UPDATE SET deleted = 1
     * - 0-未删除，1-已删除
     */
    @TableField("deleted")
    private Long deleted;
}
