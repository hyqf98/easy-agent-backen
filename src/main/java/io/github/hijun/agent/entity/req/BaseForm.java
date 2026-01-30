package io.github.hijun.agent.entity.req;

import lombok.Data;
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
public abstract class BaseForm {

    /**
     * id.
     */
    private Long id;
}
