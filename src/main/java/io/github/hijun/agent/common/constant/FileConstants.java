package io.github.hijun.agent.common.constant;

/**
 * 文件相关常量定义。
 *
 * <p>定义智能体协作流程中使用的文件类型、扩展名和 MIME 类型常量。</p>
 *
 * @author haijun
 * @date 2025-01-23
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
public interface FileConstants {

    /**
     * 文件类型常量。
     *
     * <p>用于标识智能体生成和传递的中间文件类型。</p>
     */
    class FileType {

        /**
         * 规划文件类型 - PlanningAgent 输出。
         */
        public static final String PLAN = "plan";

        /**
         * 数据文件类型 - DataCollectAgent 输出。
         */
        public static final String DATA = "data";

        /**
         * 内容文件类型 - ContentGenAgent 输出（最终文件）。
         */
        public static final String CONTENT = "content";
    }

    /**
     * 文件扩展名常量。
     *
     * <p>定义支持的文件扩展名，包含前导点号。</p>
     */
    class FileExtension {

        /**
         * Markdown 文件扩展名。
         */
        public static final String MARKDOWN = ".md";

        /**
         * HTML 文件扩展名。
         */
        public static final String HTML = ".html";

        /**
         * PowerPoint 文件扩展名。
         */
        public static final String PPT = ".pptx";

        /**
         * Word 文档扩展名。
         */
        public static final String DOCX = ".docx";
    }

    /**
     * MIME 类型常量。
     *
     * <p>用于文件下载时设置正确的 Content-Type。</p>
     */
    class MimeType {

        /**
         * Markdown MIME 类型。
         */
        public static final String MARKDOWN = "text/markdown";

        /**
         * HTML MIME 类型。
         */
        public static final String HTML = "text/html";

        /**
         * 二进制流 MIME 类型（用于 PPTX、DOCX 等文件）。
         */
        public static final String OCTET_STREAM = "application/octet-stream";
    }
}
