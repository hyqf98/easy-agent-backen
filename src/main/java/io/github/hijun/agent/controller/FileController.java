package io.github.hijun.agent.controller;

import cn.hutool.core.io.FileUtil;
import io.github.hijun.agent.common.constant.FileConstants;
import io.github.hijun.agent.config.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * 文件控制器。
 *
 * <p>提供文件访问和下载功能。</p>
 *
 * <h3>功能</h3>
 * <ul>
 *   <li>静态文件访问（支持 Markdown、HTML 等）</li>
 *   <li>会话文件下载（支持 MD/HTML/DOCX 格式）</li>
 * </ul>
 *
 * @author haijun
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    /**
     * 文件访问路径前缀.
     */
    private static final String PATH_PREFIX = "/api/files";

    /**
     * agent properties.
     */
    private final ApplicationProperties applicationProperties;

    /**
     * 访问文件
     *
     * @param filePath 文件路径
     * @return 文件资源
     * @since 1.0.0
     */
    @GetMapping("/{filePath:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filePath) {
        try {
            // 使用绝对存储路径
            String storagePath = this.applicationProperties.getStoragePath();
            // 构建安全的文件路径
            String fullPath = Paths.get(storagePath, filePath).toString();
            File file = new File(fullPath);

            // 安全检查，防止路径遍历
            String canonicalPath = file.getCanonicalPath();
            String canonicalStoragePath = new File(storagePath).getCanonicalPath();

            if (!canonicalPath.startsWith(canonicalStoragePath)) {
                log.warn("非法文件访问尝试: {}", filePath);
                return ResponseEntity.badRequest().build();
            }

            // 检查文件是否存在
            if (!file.exists() || !file.isFile()) {
                log.warn("文件不存在: {}", fullPath);
                return ResponseEntity.notFound().build();
            }
            // 检查文件类型，只允许访问特定类型的文件
            String fileExtension = this.getFileExtension(filePath);
            if (!this.isValidFileType(fileExtension)) {
                log.warn("不允许访问的文件类型: {}", fileExtension);
                return ResponseEntity.badRequest().build();
            }

            // 创建资源对象
            Resource resource = new FileSystemResource(file);

            // 根据文件扩展名设置内容类型
            MediaType contentType = this.getMediaTypeForFileExtension(fileExtension);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM;
            }

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                    .body(resource);

        } catch (IOException e) {
            log.error("文件访问失败", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param fileName 文件名
     * @return 扩展名
     * @since 1.0.0
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 检查文件类型是否有效
     *
     * @param extension 文件扩展名
     * @return 是否有效
     * @since 1.0.0
     */
    private boolean isValidFileType(String extension) {
        return extension.equals("md") || extension.equals("html") || extension.equals("txt") ||
                extension.equals("css") || extension.equals("js") || extension.equals("json");
    }

    /**
     * 根据文件扩展名获取媒体类型
     *
     * @param extension 文件扩展名
     * @return 媒体类型
     * @since 1.0.0
     */
    private MediaType getMediaTypeForFileExtension(String extension) {
        return switch (extension.toLowerCase()) {
            case "html" -> MediaType.TEXT_HTML;
            case "md", "txt" -> MediaType.TEXT_PLAIN;
            case "css" -> MediaType.valueOf("text/css");
            case "js" -> MediaType.valueOf("application/javascript");
            case "json" -> MediaType.APPLICATION_JSON;
            default -> null;
        };
    }

    /**
     * 下载会话文件。
     * <p>支持下载会话中生成的文件，支持多种格式。</p>
     *
     * @param sessionId 会话 ID
     * @param fileType 文件类型（plan/data/content），默认 content
     * @param format 下载格式（md/html/docx），默认 md
     * @return 文件响应
     * @since 1.0.0
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam String sessionId,
            @RequestParam(defaultValue = FileConstants.FileType.CONTENT) String fileType,
            @RequestParam(defaultValue = "md") String format) {

        try {
            // 验证文件类型
            if (!FileConstants.FileType.PLAN.equals(fileType) &&
                !FileConstants.FileType.DATA.equals(fileType) &&
                !FileConstants.FileType.CONTENT.equals(fileType)) {
                log.warn("无效的文件类型: {}", fileType);
                return ResponseEntity.badRequest().build();
            }

            // 构建文件路径
            String storagePath = this.applicationProperties.getStoragePath();
            String fileName = fileType + FileConstants.FileExtension.MARKDOWN;
            String fullPath = Paths.get(storagePath, sessionId, fileName).toString();
            File file = new File(fullPath);

            // 安全检查
            String canonicalPath = file.getCanonicalPath();
            String canonicalStoragePath = new File(storagePath).getCanonicalPath();

            if (!canonicalPath.startsWith(canonicalStoragePath)) {
                log.warn("非法文件访问尝试: {}/{}", sessionId, fileType);
                return ResponseEntity.badRequest().build();
            }

            // 检查文件是否存在
            if (!file.exists() || !file.isFile()) {
                log.warn("文件不存在: {}", fullPath);
                return ResponseEntity.notFound().build();
            }

            // 读取文件内容
            String content = FileUtil.readUtf8String(file);

            // 根据格式转换（当前仅支持 MD，HTML 和 DOCX 转换可扩展）
            String downloadFileName;
            MediaType mediaType;
            String downloadContent;

            if ("html".equals(format)) {
                // 简单的 Markdown 转 HTML（可使用更强大的库）
                downloadContent = convertMarkdownToHtml(content);
                downloadFileName = fileType + ".html";
                mediaType = MediaType.TEXT_HTML;
            } else if ("docx".equals(format)) {
                // DOCX 转换（需要额外库支持，当前暂返回 MD）
                log.info("DOCX 转换暂未实现，返回 Markdown 格式");
                downloadContent = content;
                downloadFileName = fileType + ".md";
                mediaType = MediaType.TEXT_PLAIN;
            } else {
                // 默认返回 Markdown
                downloadContent = content;
                downloadFileName = fileType + ".md";
                mediaType = MediaType.valueOf(FileConstants.MimeType.MARKDOWN);
            }

            // 创建临时资源
            Resource resource = new org.springframework.core.io.ByteArrayResource(
                downloadContent.getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + downloadFileName + "\"")
                    .body(resource);

        } catch (IOException e) {
            log.error("文件下载失败", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 简单的 Markdown 转 HTML。
     * <p>基础实现，可替换为更强大的转换库。</p>
     *
     * @param markdown Markdown 内容
     * @return HTML 内容
     */
    private String convertMarkdownToHtml(String markdown) {
        // 基础转换，生产环境建议使用 commonmark 或 flexmark
        String html = markdown
                .replace("### ", "<h3>")
                .replace("## ", "<h2>")
                .replace("# ", "<h1>")
                .replace("\n", "<br>")
                .replace("**", "<strong>")
                .replace("*", "<em>");
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'>" +
               "<style>body{font-family:sans-serif;padding:20px;}" +
               "h1,h2,h3{color:#333;}</style></head><body>" +
               html + "</body></html>";
    }
}
