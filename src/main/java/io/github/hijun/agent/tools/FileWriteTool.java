package io.github.hijun.agent.tools;

import cn.hutool.core.io.FileUtil;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.github.hijun.agent.config.AgentProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

/**
 * 文件写入工具
 *
 * @author haijun
 * @version 1.0.0
 * @email "mailto:haijun@email.com"
 * @date 2026/1/7 10:00
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileWriteTool {

    /**
     * path prefix.
     */
    public static final String PATH_PREFIX = "/api/files";


    /**
     * agent properties.
     */
    private final AgentProperties agentProperties;

    /**
     * 文件写入参数记录
     *
     * @author haijun
     * @version 1.0.0-SNAPSHOT
     * @email "mailto:haijun@email.com"
     * @date 2026/1/7 15:16
     * @since 1.0.0-SNAPSHOT
     */
    @JsonClassDescription("Parameters for writing content to a file")
    public record FileWriteParams(
            @JsonPropertyDescription("文件名，不含扩展名") String fileName,
            @JsonPropertyDescription("文件类型，支持 md 或 html") String fileType,
            @JsonPropertyDescription("文件内容") String content
    ) {
    }

    /**
     * 文件写入
     *
     * @param params 文件写入参数
     * @return 写入结果
     * @since 1.0.0
     */
    @Tool(description = "文件报告生成工具")
    public String fileWrite(FileWriteParams params) {
        try {
            String fileName = params.fileName;
            String content = params.content;
            String fileType = params.fileType;
            // 验证必需参数
            if (fileName == null || content == null || fileType == null) {
                throw new IllegalArgumentException("fileName, fileType, and content are required parameters");
            }

            // 验证文件类型
            if (!fileType.equalsIgnoreCase("md") && !fileType.equalsIgnoreCase("html")) {
                throw new IllegalArgumentException("Only md and html file types are supported");
            }

            // 使用绝对存储路径
            String storagePath = this.agentProperties.getStoragePath();

            // 构建完整的文件路径
            String fullPath = Paths.get(storagePath, fileName + "." + fileType).toString();

            // 确保目录存在
            FileUtil.mkParentDirs(fullPath);

            // 写入文件
            FileUtil.writeString(content, fullPath, StandardCharsets.UTF_8);

            String fileUrl = PATH_PREFIX + File.separator + fileName + "." + fileType;

            log.info("文件写入成功: {}", fullPath);
            return "文件已成功写入: " + fileUrl + ", 文件路径: " + fullPath;
        } catch (Exception e) {
            log.error("文件写入失败", e);
            return "文件写入失败: " + e.getMessage();
        }
    }
}
