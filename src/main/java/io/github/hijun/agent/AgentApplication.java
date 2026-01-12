package io.github.hijun.agent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Agent Application
 *
 * @author haijun
 * @version 1.0.0
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/1/7 10:00
 * @since 1.0.0
 */
@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class AgentApplication {

    /**
     * Main
     *
     * @param args args
     * @since 1.0.0-SNAPSHOT
     */
    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }
}
