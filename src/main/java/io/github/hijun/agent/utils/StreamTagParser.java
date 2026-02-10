package io.github.hijun.agent.utils;

import io.github.hijun.agent.common.enums.StreamTagType;

import java.util.ArrayList;
import java.util.List;

/**
 * 简易流式标签解析器 (支持单例复用版)
 * <p>
 * 原理：将"解析规则配置"与"解析运行时状态"分离。
 * Parser 本身是单例的，每次解析流时创建一个轻量级的 Session。
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
     * 策略列表 (只读配置，线程安全)
     */
    private final List<TagStrategy> strategies = new ArrayList<>();

    /**
     * Stream Tag Parser
     * 初始化时注册默认标签
     *
     * @since 1.0.0-SNAPSHOT
     */
    public StreamTagParser() {
        this.register(StreamTagType.FINAL_ANSWER)
                .register(StreamTagType.THINK);
    }

    /**
     * 注册一种新的标签解析策略 (使用枚举类型)
     * <p>支持多智能体复用相同的标签定义</p>
     *
     * @param tagType 标签类型枚举
     * @return stream tag parser
     * @since 1.0.0-SNAPSHOT
     */
    public StreamTagParser register(StreamTagType tagType) {
        this.strategies.add(new TagStrategy(tagType.getStartTag(), tagType.getEndTag(), tagType));
        return this;
    }

    /**
     * 创建一个新的解析会话 (Session)
     * <p>每次开始一个新的流式请求时，调用此方法获取一个独立的解析器状态对象</p>
     *
     * @return 独立的解析会话对象
     * @since 1.0.0-SNAPSHOT
     */
    public Session createSession() {
        return new Session(this.strategies);
    }

    /**
     * 解析会话 (Stateful)
     * <p>持有解析过程中的临时状态 (Buffer, CurrentStrategy)，非线程安全，仅限单次流请求使用</p>
     *
     * @author haijun
     * @email "mailto:iamxiaohaijun@gmail.com"
     * @date 2026/2/4 11:12
     * @version 1.0.0-SNAPSHOT
     * @since 1.0.0-SNAPSHOT
     */
    public static class Session {
        /**
         * 策略列表引用
         */
        private final List<TagStrategy> strategies;

        /**
         * 当前命中的策略 (null 表示在普通文本区)
         */
        private TagStrategy currentStrategy = null;

        /**
         * buffer (标签匹配缓冲区)
         */
        private final StringBuilder buffer = new StringBuilder();

        /**
         * text buffer (内容累积缓冲区)
         */
        private final StringBuilder textBuffer = new StringBuilder();

        /**
         * Session
         *
         * @param strategies strategies
         * @since 1.0.0-SNAPSHOT
         */
        private Session(List<TagStrategy> strategies) {
            this.strategies = strategies;
        }

        /**
         * 解析入口 (流式片段)
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
                // 如果在策略里，返回策略定义的枚举类型；否则返回 null (表示普通文本)
                StreamTagType tagType = (this.currentStrategy != null)
                        ? this.currentStrategy.tagType()
                        : null;

                results.add(new ParseResult(this.textBuffer.toString(), tagType));
                this.textBuffer.setLength(0);
            }
        }
    }

    /**
     * Tag Strategy
     *
     * @author haijun
     * @version 1.0.0-SNAPSHOT
     * @email "mailto:iamxiaohaijun@gmail.com"
     * @date 2026/2/3 18:03
     * @since 1.0.0-SNAPSHOT
     */
    private record TagStrategy(String startTag, String endTag, StreamTagType tagType) {
    }

    /**
     * Parse Result
     * <p>直接返回标签类型枚举，无需字符串判断</p>
     *
     * @author haijun
     * @version 1.0.0-SNAPSHOT
     * @email "mailto:iamxiaohaijun@gmail.com"
     * @date 2026/2/3 18:03
     * @since 1.0.0-SNAPSHOT
     */
    public record ParseResult(String content, StreamTagType tagType) {
    }
}
