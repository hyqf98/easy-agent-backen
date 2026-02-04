package io.github.hijun.agent.entity.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 会话查询
 * <p>
 * 用于会话列表查询条件
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "会话查询条件")
public class SessionQuery extends BaseQuery {

    /**
     * 标题模糊查询
     */
    @Schema(description = "标题关键词")
    private String title;

    /**
     * 模型ID筛选
     */
    @Schema(description = "模型ID")
    private Long modelId;
}
