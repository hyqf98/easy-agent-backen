package io.github.hijun.agent.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Agent
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/2/5 11:15
 * @since 1.0.0-SNAPSHOT
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Agent {

    /**
     * Value
     *
     * @return string
     * @since 1.0.0-SNAPSHOT
     */
    String value() default "";

    /**
     * Description
     *
     * @return string
     * @since 1.0.0-SNAPSHOT
     */
    String description() default "";

}
