package io.github.hijun.agent.tools.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import io.github.hijun.agent.tools.ToolDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculator Tool
 *
 * @author haijun
 * @email "mailto:haijun@email.com"
 * @date 2025/12/24 16:55
 * @version 3.4.3
 * @since 3.4.3
 */
@Slf4j
@Component
public class CalculatorTool implements ToolDefinition {

    /**
     * Get Name
     *
     * @return string
     * @since 3.4.3
     */
    @Override
    public String getName() {
        return "calculator";
    }

    /**
     * Get Description
     *
     * @return string
     * @since 3.4.3
     */
    @Override
    public String getDescription() {
        return "执行数学计算，支持加减乘除等基本运算";
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
        String expression = MapUtil.getStr(arguments, "expression");
        log.info("执行计算，表达式：{}", expression);

        if (StrUtil.isBlank(expression)) {
            return Map.of("error", "表达式不能为空");
        }

        try {
            // 简单实现：解析简单的双操作数运算
            double result = this.evaluateSimpleExpression(expression);
            return Map.of(
                    "expression", expression,
                    "result", result
            );
        } catch (Exception e) {
            log.error("计算失败", e);
            return Map.of("error", "计算失败: " + e.getMessage());
        }
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
                "expression", Map.of(
                        "type", "string",
                        "description", "数学表达式，例如：23 * 45"
                )
        ));
        schema.put("required", List.of("expression"));
        return schema;
    }

    /**
     * 简单的表达式求值
     * 支持：数字1 运算符 数字2 的格式
     *
     * @param expression 表达式
     * @return 计算结果
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
            // 没有运算符，直接解析数字
            return Double.parseDouble(expression);
        }

        // 提取操作数
        double num1 = Double.parseDouble(expression.substring(0, operatorIndex).trim());
        double num2 = Double.parseDouble(expression.substring(operatorIndex + 1).trim());

        // 执行运算
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
