package io.github.hijun.agent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Agent Tools
 *
 * @author haijun
 * @email "mailto:haijun@email.com"
 * @date 2025/12/24 17:26
 * @version 3.4.3
 * @since 3.4.3
 */
@Component
public class AgentTools {

    /**
     * r a n d o m.
     */
    private static final Random RANDOM = new Random();

    /**
     * 网页搜索工具（示例实现，使用模拟数据）
     *
     * @param query query
     * @return map
     * @since 3.4.3
     */
    @Tool(description = "搜索互联网上的信息，返回相关的搜索结果")
    public Map<String, Object> webSearch(
            @ToolParam(description = "搜索关键词") String query) {

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

    /**
     * 计算器工具
     *
     * @param expression expression
     * @return map
     * @since 3.4.3
     */
    @Tool(description = "执行数学计算，支持加减乘除等基本运算")
    public Map<String, Object> calculator(
            @ToolParam(description = "数学表达式，例如：23 * 45") String expression) {

        if (expression == null || expression.isBlank()) {
            return Map.of("error", "表达式不能为空");
        }

        try {
            double result = this.evaluateSimpleExpression(expression);
            return Map.of(
                    "expression", expression,
                    "result", result
            );
        } catch (Exception e) {
            return Map.of("error", "计算失败: " + e.getMessage());
        }
    }

    /**
     * 天气查询工具（示例实现，使用模拟数据）
     *
     * @param city city
     * @return map
     * @since 3.4.3
     */
    @Tool(description = "查询指定城市的天气情况")
    public Map<String, Object> weather(
            @ToolParam(description = "城市名称，例如：北京、上海") String city) {

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
     * 简单的表达式求值
     * 支持：数字1 运算符 数字2 的格式
     *
     * @param expression expression
     * @return double
     * @since 3.4.3
     */
    private double evaluateSimpleExpression(String expression) {
        expression = expression.trim();

        // 查找运算符
        char operator = ' ';
        int operatorIndex = -1;

        for (int i = 1; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == '+' || c == '-' || c == '*' || c == '/') {
                operator = c;
                operatorIndex = i;
                break;
            }
        }

        if (operatorIndex == -1) {
            return Double.parseDouble(expression);
        }

        double num1 = Double.parseDouble(expression.substring(0, operatorIndex).trim());
        double num2 = Double.parseDouble(expression.substring(operatorIndex + 1).trim());

        return switch (operator) {
            case '+' -> num1 + num2;
            case '-' -> num1 - num2;
            case '*' -> num1 * num2;
            case '/' -> {
                if (num2 == 0) {
                    throw new ArithmeticException("除数不能为零");
                }
                yield num1 / num2;
            }
            default -> throw new IllegalArgumentException("不支持的运算符: " + operator);
        };
    }
}
