package io.github.hijun.agent.common.constant;

/**
 * 文件相关常量定义。
 *
 * <p>定义智能体协作流程中使用的文件类型、扩展名和 MIME 类型常量。</p>
 *
 * <h3>支持的文件类型</h3>
 * <ul>
 *   <li>{@link FileType#PLAN} - plan.md：规划文件</li>
 *   <li>{@link FileType#DATA} - data.md：数据文件</li>
 *   <li>{@link FileType#CONTENT} - content.md：内容文件（最终输出）</li>
 * </ul>
 *
 * @author haijun
 * @date 2025-01-23
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
public interface FileConstants {

    /**
     * 文件类型常量。
     */
    interface FileType {

        /**
         * 规划文件类型。
         */
        String PLAN = "plan";

        /**
         * 数据文件类型。
         */
        String DATA = "data";

        /**
         * 内容文件类型。
         */
        String CONTENT = "content";

        /**
         * Markdown 文件扩展名。
         */
        String MARKDOWN_EXT = ".md";
    }

    /**
     * MIME 类型常量。
     */
    interface MimeType {

        // ========== 文本类型 ==========

        /**
         * 纯文本类型。
         */
        String TEXT_PLAIN = "text/plain";

        /**
         * HTML 类型。
         */
        String TEXT_HTML = "text/html";

        /**
         * CSS 类型。
         */
        String TEXT_CSS = "text/css";

        /**
         * JavaScript 类型。
         */
        String TEXT_JAVASCRIPT = "text/javascript";

        /**
         * Markdown 类型。
         */
        String TEXT_MARKDOWN = "text/markdown";

        /**
         * XML 类型。
         */
        String TEXT_XML = "text/xml";

        // ========== 图片类型 ==========

        /**
         * JPEG 图片类型。
         */
        String IMAGE_JPEG = "image/jpeg";

        /**
         * PNG 图片类型。
         */
        String IMAGE_PNG = "image/png";

        /**
         * GIF 图片类型。
         */
        String IMAGE_GIF = "image/gif";

        /**
         * SVG 图片类型。
         */
        String IMAGE_SVG = "image/svg+xml";

        /**
         * WebP 图片类型。
         */
        String IMAGE_WEBP = "image/webp";

        /**
         * ICO 图片类型。
         */
        String IMAGE_ICO = "image/x-icon";

        /**
         * BMP 图片类型。
         */
        String IMAGE_BMP = "image/bmp";

        // ========== 音频类型 ==========

        /**
         * MP3 音频类型。
         */
        String AUDIO_MP3 = "audio/mpeg";

        /**
         * WAV 音频类型。
         */
        String AUDIO_WAV = "audio/wav";

        /**
         * OGG 音频类型。
         */
        String AUDIO_OGG = "audio/ogg";

        // ========== 视频类型 ==========

        /**
         * MP4 视频类型。
         */
        String VIDEO_MP4 = "video/mp4";

        /**
         * WebM 视频类型。
         */
        String VIDEO_WEBM = "video/webm";

        /**
         * AVI 视频类型。
         */
        String VIDEO_AVI = "video/x-msvideo";

        // ========== 应用类型 ==========

        /**
         * JSON 类型。
         */
        String APPLICATION_JSON = "application/json";

        /**
         * PDF 类型。
         */
        String APPLICATION_PDF = "application/pdf";

        /**
         * ZIP 压缩包类型。
         */
        String APPLICATION_ZIP = "application/zip";

        /**
         * Word 文档类型（.docx）。
         */
        String APPLICATION_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

        /**
         * Excel 表格类型（.xlsx）。
         */
        String APPLICATION_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        /**
         * PowerPoint 演示文稿类型（.pptx）。
         */
        String APPLICATION_PPTX = "application/vnd.openxmlformats-officedocument.presentationml.presentation";

        /**
         * 二进制流类型（下载时使用）。
         */
        String APPLICATION_OCTET_STREAM = "application/octet-stream";
    }

    /**
     * 文件扩展名与 MIME 类型映射。
     * <p>
     * 常用文件扩展名对应的 MIME 类型，用于静态资源访问时设置 Content-Type
     * </p>
     */
    interface ExtensionMimeType {

        /**
         * 根据文件扩展名获取 MIME 类型。
         *
         * @param extension 文件扩展名（包含点号，如 ".jpg"）
         * @return MIME 类型，未知类型返回 {@link MimeType#APPLICATION_OCTET_STREAM}
         */
        static String getMimeType(String extension) {
            if (extension == null || extension.isBlank()) {
                return MimeType.APPLICATION_OCTET_STREAM;
            }

            String ext = extension.toLowerCase();
            return switch (ext) {
                // ========== 文本类型 ==========
                case ".txt", ".log", ".conf", ".ini", ".properties" -> MimeType.TEXT_PLAIN;
                case ".html", ".htm" -> MimeType.TEXT_HTML;
                case ".css" -> MimeType.TEXT_CSS;
                case ".js" -> MimeType.TEXT_JAVASCRIPT;
                case ".md" -> MimeType.TEXT_MARKDOWN;
                case ".xml" -> MimeType.TEXT_XML;

                // ========== 图片类型 ==========
                case ".jpg", ".jpeg" -> MimeType.IMAGE_JPEG;
                case ".png" -> MimeType.IMAGE_PNG;
                case ".gif" -> MimeType.IMAGE_GIF;
                case ".svg", ".svgz" -> MimeType.IMAGE_SVG;
                case ".webp" -> MimeType.IMAGE_WEBP;
                case ".ico" -> MimeType.IMAGE_ICO;
                case ".bmp" -> MimeType.IMAGE_BMP;

                // ========== 音频类型 ==========
                case ".mp3" -> MimeType.AUDIO_MP3;
                case ".wav" -> MimeType.AUDIO_WAV;
                case ".ogg" -> MimeType.AUDIO_OGG;

                // ========== 视频类型 ==========
                case ".mp4", ".m4v" -> MimeType.VIDEO_MP4;
                case ".webm" -> MimeType.VIDEO_WEBM;
                case ".avi" -> MimeType.VIDEO_AVI;

                // ========== 应用类型 ==========
                case ".json" -> MimeType.APPLICATION_JSON;
                case ".pdf" -> MimeType.APPLICATION_PDF;
                case ".zip" -> MimeType.APPLICATION_ZIP;
                case ".docx" -> MimeType.APPLICATION_DOCX;
                case ".xlsx" -> MimeType.APPLICATION_XLSX;
                case ".pptx" -> MimeType.APPLICATION_PPTX;

                // ========== 默认类型 ==========
                default -> MimeType.APPLICATION_OCTET_STREAM;
            };
        }
    }
}
