package io.github.hijun.agent.tools.impl;

import cn.hutool.core.map.MapUtil;
import io.github.hijun.agent.tools.ToolDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Weather Tool
 *
 * @author haijun
 * @email "mailto:haijun@email.com"
 * @date 2025/12/24 16:55
 * @version 3.4.3
 * @since 3.4.3
 */
@Slf4j
@Component
public class WeatherTool implements ToolDefinition {

    /**
     * r a n d o m.
     */
    private static final Random RANDOM = new Random();

    /**
     * Get Name
     *
     * @return string
     * @since 3.4.3
     */
    @Override
    public String getName() {
        return "weather";
    }

    /**
     * Get Description
     *
     * @return string
     * @since 3.4.3
     */
    @Override
    public String getDescription() {
        return "查询指定城市的天气情况";
    }

    /**
     * Execute
     *
     * @param arguments arguments
     * @return object
     * @since 3.4.3
     */
    @Override
    public Object execute(Map<String, Object> arguments) {
        String city = MapUtil.getStr(arguments, "city");
        log.info("查询天气，城市：{}", city);

        // MVP阶段：返回模拟数据
        int temperature = RANDOM.nextInt(35) - 5; // -5°C 到 30°C
        String[] conditions = {"晴", "多云", "阴", "小雨", "大雨"};
        String condition = conditions[RANDOM.nextInt(conditions.length)];

        return Map.of(
                "city", city,
                "temperature", temperature,
                "condition", condition,
                "humidity", RANDOM.nextInt(100),
                "windSpeed", RANDOM.nextInt(30),
                "updateTime", System.currentTimeMillis()
        );
    }

    /**
     * Get Parameters Schema
     *
     * @return map
     * @since 3.4.3
     */
    @Override
    public Map<String, Object> getParametersSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of(
                "city", Map.of(
                        "type", "string",
                        "description", "城市名称，例如：北京、上海"
                )
        ));
        schema.put("required", List.of("city"));
        return schema;
    }
}
