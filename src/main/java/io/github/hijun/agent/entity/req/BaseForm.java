package io.github.hijun.agent.entity.req;

import io.github.hijun.agent.common.validation.UpdateGroup;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base Form
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/1/30 13:32
 * @since 1.0.0-SNAPSHOT
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseForm {

    /**
     * id.
     */
    @NotNull(groups = UpdateGroup.class)
    private Long id;
}
