package io.github.hijun.agent.utils;

import io.github.hijun.agent.common.constant.AgentConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * 简易流式标签解析器 (无泛型版)
 * <p>
 * 使用 String 来标识消息类型，简单直观。
 * </p>
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/2/3 18:03
 * @since 1.0.0-SNAPSHOT
 */
public class StreamTagParser {

    /**
     * 默认的普通文本类型名称
     */
    public static final String TYPE_CONTENT = "CONTENT";

    /**
     * 策略列表
     */
    private final List<TagStrategy> strategies = new ArrayList<>();

    /**
     * 当前命中的策略 (null 表示在普通文本区)
     */
    private TagStrategy currentStrategy = null;

    /**
     * buffer.
     */
    private final StringBuilder buffer = new StringBuilder();
    /**
     * text buffer.
     */
    private final StringBuilder textBuffer = new StringBuilder();

    /**
     * Stream Tag Parser
     *
     * @since 1.0.0-SNAPSHOT
     */
    public StreamTagParser() {
        this.register("<Final Answer>", "</Final Answer>", AgentConstants.AgentContentType.FINAL_ANSWER)
                .register("<think>", "</think>", "THINK");
    }

    /**
     * 注册一种新的标签解析策略
     *
     * @param startTag 开始标签，如 "<Final Answer>"
     * @param endTag   结束标签，如 "</Final Answer>"
     * @param typeName 对应的类型名称，如 "FINAL_ANSWER"
     * @return stream tag parser
     * @since 1.0.0-SNAPSHOT
     */
    public StreamTagParser register(String startTag, String endTag, String typeName) {
        this.strategies.add(new TagStrategy(startTag, endTag, typeName));
        return this;
    }

    /**
     * 解析入口
     *
     * @param chunk 流式输入的一个片段
     * @return 解析结果列表
     * @since 1.0.0-SNAPSHOT
     */
    public List<ParseResult> parse(String chunk) {
        List<ParseResult> results = new ArrayList<>();
        if (chunk == null || chunk.isEmpty()) {
            return results;
        }

        for (char c : chunk.toCharArray()) {
            this.processChar(c, results);
        }
        this.flushTextBuffer(results);
        return results;
    }

    // --- 内部核心逻辑 (和之前一样，只是把 T 换成了 String) ---

    /**
     * Process Char
     *
     * @param c       c
     * @param results results
     * @since 1.0.0-SNAPSHOT
     */
    private void processChar(char c, List<ParseResult> results) {
        String potentialTag = this.buffer.toString() + c;

        boolean isPrefix = false;
        boolean isExactMatch = false;
        TagStrategy matchedStrategy = null;

        if (this.currentStrategy != null) {
            // 在标签内：等待结束标签
            String target = this.currentStrategy.endTag();
            if (target.equals(potentialTag)) {
                isExactMatch = true;
            } else if (target.startsWith(potentialTag)) {
                isPrefix = true;
            }
        } else {
            // 在标签外：等待任意开始标签
            for (TagStrategy strategy : this.strategies) {
                if (strategy.startTag().equals(potentialTag)) {
                    isExactMatch = true;
                    matchedStrategy = strategy;
                    break;
                } else if (strategy.startTag().startsWith(potentialTag)) {
                    isPrefix = true;
                }
            }
        }

        if (isExactMatch) {
            this.buffer.append(c);
            this.flushTextBuffer(results); // 状态改变前，输出旧积攒的文本

            // 切换状态
            if (this.currentStrategy != null) {
                this.currentStrategy = null; // 退出标签
            } else {
                this.currentStrategy = matchedStrategy; // 进入标签
            }

            this.buffer.setLength(0); // 消费掉标签字符
        } else if (isPrefix) {
            this.buffer.append(c); // 继续匹配前缀
        } else {
            // 匹配失败
            if (!this.buffer.isEmpty()) {
                // 刚才以为是标签的字符其实是普通文本，转移到 textBuffer
                this.textBuffer.append(this.buffer);
                this.buffer.setLength(0);
                // 递归重试当前字符
                this.processChar(c, results);
            } else {
                this.textBuffer.append(c);
            }
        }
    }

    /**
     * Flush Text Buffer
     *
     * @param results results
     * @since 1.0.0-SNAPSHOT
     */
    private void flushTextBuffer(List<ParseResult> results) {
        if (!this.textBuffer.isEmpty()) {
            // 如果在策略里，返回策略定义的类型；否则返回默认 CONTENT
            String type = (this.currentStrategy != null)
                    ? this.currentStrategy.type()
                    : TYPE_CONTENT;

            results.add(new ParseResult(this.textBuffer.toString(), type));
            this.textBuffer.setLength(0);
        }
    }

    // --- 简单的数据载体类 ---

    /**
     * Tag Strategy
     *
     * @author haijun
     * @version 1.0.0-SNAPSHOT
     * @email "mailto:iamxiaohaijun@gmail.com"
     * @date 2026/2/3 18:03
     * @since 1.0.0-SNAPSHOT
     */
    private record TagStrategy(String startTag, String endTag, String type) {
    }

    /**
     * Parse Result
     *
     * @author haijun
     * @version 1.0.0-SNAPSHOT
     * @email "mailto:iamxiaohaijun@gmail.com"
     * @date 2026/2/3 18:03
     * @since 1.0.0-SNAPSHOT
     */
    public record ParseResult(String content, String type) {
    }
}
