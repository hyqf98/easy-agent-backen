package io.github.hijun.agent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import io.github.hijun.agent.common.constant.FileConstants;
import io.github.hijun.agent.config.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 文件操作工具。
 *
 * <p>提供 AI 模型读写文件的能力，用于智能体之间通过文件传递数据。</p>
 *
 * <h3>文件存储结构</h3>
 * <pre>
 * {storagePath}/
 *   └── {sessionId}/
 *       ├── plan.md      (PlanningAgent 输出)
 *       ├── data.md      (DataCollectAgent 输出)
 *       └── content.md   (ContentGenAgent 输出，最终文件)
 * </pre>
 *
 * <h3>支持的文件类型</h3>
 * <ul>
 *   <li>{@link FileConstants.FileType#PLAN} - plan.md：规划文件</li>
 *   <li>{@link FileConstants.FileType#DATA} - data.md：数据文件</li>
 *   <li>{@link FileConstants.FileType#CONTENT} - content.md：内容文件（最终输出）</li>
 * </ul>
 *
 * <h3>工具方法</h3>
 * <ul>
 *   <li>{@link #readFile(String)} - 读取文件内容</li>
 *   <li>{@link #writeFile(String, String)} - 写入文件到存储路径</li>
 *   <li>{@link #writeFileInSession(String, String, String)} - 在会话目录下写入文件</li>
 * </ul>
 *
 * <h3>错误处理</h3>
 * <ul>
 *   <li>参数校验：抛出 {@link IllegalArgumentException}</li>
 *   <li>IO 异常：抛出 {@link RuntimeException} 包装原始异常</li>
 *   <li>所有方法都有完整的日志记录</li>
 * </ul>
 *
 * @author hijun
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileTools {

    /**
     * 应用配置属性.
     */
    private final ApplicationProperties applicationProperties;

    /**
     * 文件存储基础路径.
     */
    private static final String DEFAULT_STORAGE_PATH = "/tmp/agent-files/";

    /**
     * 读取文件内容.
     * <p>读取指定路径的文件，返回其文本内容</p>
     *
     * @param filePath 文件路径，支持相对于存储路径的相对路径或绝对路径
     * @return 文件内容的字符串表示
     * @throws IllegalArgumentException 当文件路径为空时
     * @throws RuntimeException 当文件不存在或读取失败时
     */
    @Tool(description = "读取指定路径的文件内容，返回文件文本内容")
    public String readFile(
            @ToolParam(description = "要读取的文件路径，支持相对路径和绝对路径") String filePath) {

        if (StrUtil.isBlank(filePath)) {
            throw new IllegalArgumentException("文件路径不能为空");
        }

        // 处理相对路径
        String fullPath = resolvePath(filePath);

        log.info("读取文件: {}", fullPath);

        try {
            String content = FileUtil.readUtf8String(fullPath);
            log.info("文件读取成功，内容长度: {}", content.length());
            return content;
        } catch (Exception e) {
            log.error("文件读取失败: {}", fullPath, e);
            throw new RuntimeException("文件读取失败: " + e.getMessage(), e);
        }
    }

    /**
     * 写入文件内容.
     * <p>将内容写入指定文件，如果父目录不存在则自动创建</p>
     *
     * @param fileName 文件名称（包含扩展名，如 plan.md）
     * @param content  文件内容
     * @return 文件的完整绝对路径
     * @throws IllegalArgumentException 当文件名为空时
     * @throws RuntimeException 当文件写入失败时
     */
    @Tool(description = "将内容写入文件，返回文件完整路径")
    public String writeFile(
            @ToolParam(description = "文件名称，如 plan.md、data.md、content.md") String fileName,
            @ToolParam(description = "要写入的文件内容") String content) {

        if (StrUtil.isBlank(fileName)) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String storagePath = getStoragePath();
        String fullPath = storagePath + fileName;

        log.info("写入文件: {}", fullPath);

        try {
            // 确保父目录存在
            FileUtil.mkParentDirs(fullPath);

            // 写入文件
            FileUtil.writeString(content, fullPath, StandardCharsets.UTF_8);

            log.info("文件写入成功: {}", fullPath);
            return fullPath;
        } catch (Exception e) {
            log.error("文件写入失败: {}", fullPath, e);
            throw new RuntimeException("文件写入失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建会话目录并写入文件。
     * <p>为指定会话创建独立目录，并将文件写入其中。</p>
     *
     * <p>文件路径格式：{storagePath}{sessionId}/{fileType}.md</p>
     *
     * <p>支持的文件类型（使用 {@link FileConstants.FileType} 常量）：</p>
     * <ul>
     *   <li>{@link FileConstants.FileType#PLAN} - 规划文件</li>
     *   <li>{@link FileConstants.FileType#DATA} - 数据文件</li>
     *   <li>{@link FileConstants.FileType#CONTENT} - 内容文件</li>
     * </ul>
     *
     * @param sessionId 会话 ID，用于隔离不同会话的文件
     * @param fileType  文件类型，使用 {@link FileConstants.FileType} 常量
     * @param content   要写入的文件内容
     * @return 文件的完整绝对路径
     * @throws IllegalArgumentException 当会话 ID 或文件类型为空时
     * @throws RuntimeException 当文件写入失败时
     */
    @Tool(description = "在会话目录下创建文件并写入内容，文件类型使用 plan/data/content 常量")
    public String writeFileInSession(
            @ToolParam(description = "会话ID，用于隔离不同会话的文件") String sessionId,
            @ToolParam(description = "文件类型：plan、data、content（建议使用常量）") String fileType,
            @ToolParam(description = "要写入的文件内容") String content) {

        if (StrUtil.isBlank(sessionId) || StrUtil.isBlank(fileType)) {
            throw new IllegalArgumentException("会话ID和文件类型不能为空");
        }

        // 构建文件名
        String fileName = fileType + ".md";
        String storagePath = getStoragePath();
        String sessionPath = storagePath + sessionId + "/";
        String fullPath = sessionPath + fileName;

        log.info("在会话目录写入文件: sessionId={}, fileType={}", sessionId, fileType);

        try {
            // 确保会话目录存在
            FileUtil.mkdir(sessionPath);

            // 写入文件
            FileUtil.writeString(content, fullPath, StandardCharsets.UTF_8);

            log.info("会话文件写入成功: {}", fullPath);
            return fullPath;
        } catch (Exception e) {
            log.error("会话文件写入失败: {}", fullPath, e);
            throw new RuntimeException("会话文件写入失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析文件路径.
     * <p>如果是相对路径，则基于存储路径解析；如果是绝对路径，直接返回</p>
     *
     * @param path 原始路径
     * @return 解析后的完整路径
     */
    private String resolvePath(String path) {
        if (FileUtil.isAbsolutePath(path)) {
            return path;
        }
        return getStoragePath() + path;
    }

    /**
     * 获取存储路径.
     *
     * @return 存储路径
     */
    private String getStoragePath() {
        String path = applicationProperties.getStoragePath();
        if (StrUtil.isBlank(path)) {
            path = DEFAULT_STORAGE_PATH;
        }
        if (!path.endsWith("/")) {
            path += "/";
        }
        return path;
    }
}
