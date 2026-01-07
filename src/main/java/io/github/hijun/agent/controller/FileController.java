package io.github.hijun.agent.controller;

import io.github.hijun.agent.config.AgentProperties;
import io.github.hijun.agent.tools.FileWriteTool;
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
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * File Controller - 提供静态文件访问接口
 *
 * @author haijun
 * @version 1.0.0
 * @email "mailto:haijun@email.com"
 * @date 2026/1/7 10:15
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping(FileWriteTool.PATH_PREFIX)
@RequiredArgsConstructor
public class FileController {

    /**
     * agent properties.
     */
    private final AgentProperties agentProperties;

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
            String storagePath = this.agentProperties.getStoragePath();
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
}
