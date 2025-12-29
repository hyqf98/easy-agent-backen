package io.github.hijun.agent.tools.impl;

import cn.hutool.core.map.MapUtil;
import io.github.hijun.agent.tools.ToolDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 网页搜索工具（示例实现，使用模拟数据）
 *
 * @author haijun
 * @date 2025-12-24
 */
@Slf4j
@Component
public class WebSearchTool implements ToolDefinition {

    @Override
    public String getName() {
        return "web_search";
    }

    @Override
    public String getDescription() {
        return "搜索互联网上的信息，返回相关的搜索结果";
    }

    @Override
    public Object execute(Map<String, Object> arguments) {
        String query = MapUtil.getStr(arguments, "query");
        log.info("执行网页搜索，查询关键词：{}", query);

        // MVP阶段：返回模拟数据
        return Map.of(
                "query", query,
                "count", 2,
                "results", List.of(
                        Map.of(
                                "title", "Spring AI 官方文档",
                                "url", "https://docs.spring.io/spring-ai/reference/",
                                "snippet", "Spring AI 是一个为 AI 工程提供的 Spring 生态系统项目，提供了与各种 AI 服务的集成。"
                        ),
                        Map.of(
                                "title", "Spring AI GitHub 仓库",
                                "url", "https://github.com/spring-projects/spring-ai",
                                "snippet", "Spring AI 项目的官方代码仓库，包含示例代码和详细的使用指南。"
                        )
                )
        );
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of(
                "query", Map.of(
                        "type", "string",
                        "description", "搜索关键词"
                )
        ));
        schema.put("required", List.of("query"));
        return schema;
    }
}
