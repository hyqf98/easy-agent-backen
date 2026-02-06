package io.github.hijun.agent.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import io.github.hijun.agent.common.constant.FileConstants;
import io.github.hijun.agent.config.ApplicationProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件服务控制器。
 * <p>提供静态资源文件的读取和下载功能，支持图片、文档等各种文件类型。</p>
 *
 * <h3>功能说明</h3>
 * <ul>
 * <li>根据相对路径读取文件内容</li>
 * <li>支持浏览器内预览（inline）</li>
 * <li>支持文件下载（attachment）</li>
 * <li>自动识别 MIME 类型</li>
 * <li>路径安全校验，防止目录遍历攻击</li>
 * </ul>
 *
 * <h3>使用方式</h3>
 * <ul>
 * <li>预览图片/文档：GET /file/view?path=session-123/plan.md</li>
 * <li>下载文件：GET /file/download?path=session-123/report.pdf</li>
 * </ul>
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/2/6
 * @since 1.0.0-SNAPSHOT
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@Tag(name = "文件服务", description = "静态资源文件读取和下载接口")
public class FileController {

    /**
     * 应用配置属性。
     */
    private final ApplicationProperties applicationProperties;

    // ==================== 公共方法 ====================

    /**
     * 获取文件存储根路径。
     *
     * @return 存储根路径（确保以 / 结尾）
     * @since 1.0.0-SNAPSHOT
     */
    private String getStoragePath() {
        String path = this.applicationProperties.getStoragePath();
        if (StrUtil.isBlank(path)) {
            path = ApplicationProperties.DEFAULT_STORAGE_PATH;
        }
        if (!path.endsWith("/") && !path.endsWith("\\")) {
            path += "/";
        }
        return path;
    }

    /**
     * 解析文件完整路径。
     *
     * @param relativePath 相对路径
     * @return 完整绝对路径
     * @since 1.0.0-SNAPSHOT
     */
    private String resolveFullPath(String relativePath) {
        Assert.hasText(relativePath, "文件路径不能为空");
        this.validatePathSecurity(relativePath);

        String normalizedPath = relativePath.startsWith("/") || relativePath.startsWith("\\")
                ? relativePath.substring(1)
                : relativePath;

        return this.getStoragePath() + normalizedPath;
    }

    /**
     * 获取并验证文件（统一文件校验逻辑）。
     *
     * @param relativePath 相对路径
     * @param checkExists  是否检查文件存在
     * @return 文件对象
     * @since 1.0.0-SNAPSHOT
     */
    private File getAndValidateFile(String relativePath, boolean checkExists) {
        String fullPath = this.resolveFullPath(relativePath);
        File file = new File(fullPath);

        if (checkExists && !file.exists()) {
            log.warn("文件不存在: {}", fullPath);
            throw new IllegalArgumentException("文件不存在: " + relativePath);
        }

        if (file.isDirectory()) {
            log.warn("路径是目录而非文件: {}", fullPath);
            throw new IllegalArgumentException("路径是目录而非文件: " + relativePath);
        }

        return file;
    }

    /**
     * 获取文件的 MIME 类型。
     *
     * @param filePath 文件路径
     * @return MIME 类型
     * @since 1.0.0-SNAPSHOT
     */
    private String getMimeType(String filePath) {
        String extension = FileUtil.extName(filePath);
        if (StrUtil.isBlank(extension)) {
            return FileConstants.MimeType.APPLICATION_OCTET_STREAM;
        }
        return FileConstants.ExtensionMimeType.getMimeType("." + extension);
    }

    /**
     * 将文件内容写入 HTTP 响应。
     *
     * @param file     文件对象
     * @param response HTTP 响应
     * @throws IOException 当写入失败时
     * @since 1.0.0-SNAPSHOT
     */
    private void writeFileToResponse(File file, HttpServletResponse response) throws IOException {
        Path path = Paths.get(file.getAbsolutePath());
        response.setContentLengthLong(Files.size(path));

        try (OutputStream os = response.getOutputStream()) {
            Files.copy(path, os);
            os.flush();
        }
    }

    /**
     * 路径安全校验。
     *
     * @param path 路径
     * @since 1.0.0-SNAPSHOT
     */
    private void validatePathSecurity(String path) {
        if (path.contains("..") || path.contains("../") || path.contains("..\\")) {
            throw new IllegalArgumentException("文件路径包含非法字符");
        }
    }

    // ==================== 接口方法 ====================

    /**
     * 上传文件。
     *
     * @param file      上传的文件
     * @param sessionId 会话ID
     * @return 文件上传信息
     * @throws IOException 当文件保存失败时
     * @since 1.0.0-SNAPSHOT
     */
    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传文件到服务器并返回文件相对路径")
    public FileInfo uploadFile(
            @Parameter(description = "上传的文件", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "会话ID", required = true)
            @RequestParam("sessionId") String sessionId) throws IOException {

        Assert.notNull(file, "文件不能为空");
        Assert.isTrue(!file.isEmpty(), "文件内容不能为空");
        Assert.hasText(sessionId, "会话ID不能为空");
        this.validatePathSecurity(sessionId);

        String originalFilename = file.getOriginalFilename();
        Assert.hasText(originalFilename, "文件名不能为空");
        this.validatePathSecurity(originalFilename);

        String timestamp = String.valueOf(System.currentTimeMillis());
        String safeFileName = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        String uniqueFileName = timestamp + "_" + safeFileName;
        String relativePath = sessionId + "/upload/" + uniqueFileName;
        String fullPath = this.getStoragePath() + relativePath;

        File targetFile = new File(fullPath);
        File parentDir = targetFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        file.transferTo(targetFile);

        log.info("文件上传成功: {} -> {}", originalFilename, relativePath);

        return FileInfo.builder()
                .path(relativePath)
                .fileName(originalFilename)
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .build();
    }

    /**
     * 预览文件（浏览器内显示）。
     *
     * @param path     相对路径
     * @param response HTTP 响应
     * @throws IOException 当文件读取失败时
     * @since 1.0.0-SNAPSHOT
     */
    @GetMapping("/view")
    @Operation(summary = "预览文件", description = "在浏览器内预览文件（图片、PDF等）")
    public void viewFile(
            @Parameter(description = "文件相对路径", required = true, example = "session-123/plan.md")
            @RequestParam String path,
            HttpServletResponse response) throws IOException {

        File file = this.getAndValidateFile(path, true);
        String mimeType = this.getMimeType(file.getAbsolutePath());

        log.info("预览文件: {}", path);

        response.setContentType(mimeType);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + URLEncoder.encode(file.getName(), StandardCharsets.UTF_8));
        this.writeFileToResponse(file, response);
    }

    /**
     * 下载文件。
     *
     * @param path     相对路径
     * @param response HTTP 响应
     * @throws IOException 当文件读取失败时
     * @since 1.0.0-SNAPSHOT
     */
    @GetMapping("/download")
    @Operation(summary = "下载文件", description = "下载文件到本地")
    public void downloadFile(
            @Parameter(description = "文件相对路径", required = true, example = "session-123/report.pdf")
            @RequestParam String path,
            HttpServletResponse response) throws IOException {

        File file = this.getAndValidateFile(path, true);
        String mimeType = this.getMimeType(file.getAbsolutePath());

        log.info("下载文件: {}", path);

        response.setContentType(mimeType);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + URLEncoder.encode(file.getName(), StandardCharsets.UTF_8));
        this.writeFileToResponse(file, response);
    }

    /**
     * 删除文件。
     *
     * @param path 相对路径
     * @since 1.0.0-SNAPSHOT
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除文件", description = "根据相对路径删除服务器上的文件")
    public void deleteFile(
            @Parameter(description = "文件相对路径", required = true, example = "session-123/upload/1234567890_image.jpg")
            @RequestParam String path) {

        File file = this.getAndValidateFile(path, false);

        if (!file.exists()) {
            log.warn("文件不存在，跳过删除: {}", path);
            return;
        }

        if (!file.delete()) {
            log.error("文件删除失败: {}", path);
            throw new RuntimeException("文件删除失败");
        }

        log.info("文件删除成功: {}", path);
    }

    /**
     * 读取文件内容（JSON 格式）。
     *
     * @param path 相对路径
     * @return 文件信息
     * @throws IOException 当文件读取失败时
     * @since 1.0.0-SNAPSHOT
     */
    @GetMapping("/read")
    @Operation(summary = "读取文件内容", description = "读取文本文件内容并返回")
    public FileInfo readFile(
            @Parameter(description = "文件相对路径", required = true, example = "session-123/plan.md")
            @RequestParam String path) throws IOException {

        File file = this.getAndValidateFile(path, true);

        log.info("读取文件内容: {}", path);

        return FileInfo.builder()
                .path(path)
                .fileName(file.getName())
                .fileSize(file.length())
                .mimeType(this.getMimeType(file.getAbsolutePath()))
                .content(FileUtil.readUtf8String(file))
                .build();
    }

    /**
     * 检查文件是否存在。
     *
     * @param path 相对路径
     * @return 文件存在信息
     * @since 1.0.0-SNAPSHOT
     */
    @GetMapping("/exists")
    @Operation(summary = "检查文件是否存在", description = "检查指定路径的文件是否存在")
    public FileExistsInfo checkExists(
            @Parameter(description = "文件相对路径", required = true, example = "session-123/plan.md")
            @RequestParam String path) {

        String fullPath = this.resolveFullPath(path);
        File file = new File(fullPath);
        boolean exists = file.exists() && file.isFile();

        log.info("检查文件存在性: {}, 存在: {}", path, exists);

        return FileExistsInfo.builder()
                .path(path)
                .exists(exists)
                .build();
    }

    // ==================== 内部类 ====================

    /**
     * 文件信息。
     *
     * @author haijun
     * @email "mailto:iamxiaohaijun@gmail.com"
     * @date 2026/2/6 11:36
     * @version 1.0.0-SNAPSHOT
     * @since 1.0.0-SNAPSHOT
     */
    @Builder
    @Data
    public static class FileInfo {
        /**
         * 文件相对路径。
         */
        private String path;

        /**
         * 文件名。
         */
        private String fileName;

        /**
         * 文件大小（字节）。
         */
        private Long fileSize;

        /**
         * MIME 类型。
         */
        private String mimeType;

        /**
         * 文件内容（文本文件）。
         */
        private String content;
    }

    /**
     * 文件存在信息。
     *
     * @author haijun
     * @email "mailto:iamxiaohaijun@gmail.com"
     * @date 2026/2/6 11:36
     * @version 1.0.0-SNAPSHOT
     * @since 1.0.0-SNAPSHOT
     */
    @Builder
    @Data
    public static class FileExistsInfo {
        /**
         * 文件相对路径。
         */
        private String path;

        /**
         * 文件是否存在。
         */
        private Boolean exists;
    }
}
