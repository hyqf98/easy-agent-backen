package io.github.hijun.agent.common;

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
 * @date 2026/1/13 14:10
 * @since 1.0.0-SNAPSHOT
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Agent {

    /**
     * Id
     *
     * @return string
     * @since 1.0.0-SNAPSHOT
     */
    String id();

    /**
     * Name
     *
     * @return string
     * @since 1.0.0-SNAPSHOT
     */
    String name();

    /**
     * Description
     *
     * @return string
     * @since 1.0.0-SNAPSHOT
     */
    String description();
}
