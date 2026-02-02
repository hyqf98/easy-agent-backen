package io.github.hijun.agent.entity.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

/**
 * 基础查询条件类
 * <p>
 * 所有查询条件的基类，包含分页参数和时间范围查询参数
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/1/30 13:32
 * @since 1.0.0-SNAPSHOT
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseQuery {

    /**
     * 当前页码
     */
    @Builder.Default
    private Long pageNum = 1L;

    /**
     * 每页大小
     */
    @Builder.Default
    private Long pageSize = 10L;

    /**
     * 开始时间（创建时间范围）
     */
    private Date startTime;

    /**
     * 结束时间（创建时间范围）
     */
    private Date endTime;

}
