package io.github.hijun.agent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import io.github.hijun.agent.config.ApplicationProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件操作工具。
 *
 * <p>提供 AI 模型读写文件的能力，用于智能体之间通过文件传递数据。</p>
 *
 * <h3>文件存储结构</h3>
 * <pre>
 * {storagePath}/
 * └── {sessionId}/
 * ├── plan.md      (PlanningAgent 输出)
 * ├── data.md      (DataCollectAgent 输出)
 * └── content.md   (ContentGenAgent 输出，最终文件)
 * </pre>
 *
 * <h3>核心工具方法</h3>
 * <ul>
 * <li>{@link #readFile(String)} - 读取文件内容</li>
 * <li>{@link #queryFile(String, String)} - 高级查询文件内容</li>
 * <li>{@link #writeFile(String, String)} - 写入文件</li>
 * <li>{@link #appendFile(String, String)} - 追加内容到文件</li>
 * <li>{@link #writeFileInSession(String, String, String)} - 在会话目录下写入文件</li>
 * <li>{@link #listFiles(String, Boolean, String)} - 列出目录文件</li>
 * <li>{@link #deleteFile(String)} - 删除文件或目录</li>
 * <li>{@link #fileExists(String)} - 检查文件是否存在</li>
 * <li>{@link #createDirectory(String)} - 创建目录</li>
 * </ul>
 *
 * @author hijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/2/6 11:35
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileTools {

    /**
     * 应用配置属性。
     */
    private final ApplicationProperties applicationProperties;

    /**
     * 读取文件内容。
     *
     * @param filePath 文件路径，支持相对路径和绝对路径
     * @return 文件内容
     * @since 1.0.0-SNAPSHOT
     */
    @Tool(description = """
            读取文件的全部内容。
            支持相对路径（基于配置的存储根路径）和绝对路径。
            常用于读取配置文件、数据文件、会话文件等。
            """)
    public String readFile(
            @ToolParam(description = "文件路径，如 session-123/plan.md") String filePath) {

        if (StrUtil.isBlank(filePath)) {
            throw new IllegalArgumentException("文件路径不能为空");
        }

        String fullPath = this.resolvePath(filePath);
        log.info("读取文件: {}", fullPath);

        try {
            return FileUtil.readUtf8String(fullPath);
        } catch (Exception e) {
            log.error("文件读取失败: {}", fullPath, e);
            throw new RuntimeException("文件读取失败: " + e.getMessage(), e);
        }
    }

    /**
     * 高级查询文件内容。
     * <p>支持关键词搜索、正则匹配、行范围过滤、上下文显示等</p>
     *
     * @param filePath 文件路径
     * @param options  查询选项（JSON格式）
     * @return 匹配的内容
     * @since 1.0.0-SNAPSHOT
     */
    @Tool(description = """
            高级查询文件内容，支持多种过滤条件。

            参数说明：
            - filePath: 文件路径（必填）
            - options: 查询选项（JSON字符串，可选）

            查询选项（options）：
            {
              "startLine": 1,              // 起始行号（默认1）
              "endLine": 100,              // 结束行号（默认文件末尾）
              "keyword": "TODO",           // 关键词搜索
              "ignoreCase": true,          // 忽略大小写（默认true）
              "exclude": "注释",           // 排除包含此关键词的行
              "regex": "\\d{4}-\\d{2}",   // 正则表达式匹配
              "context": 2,                // 上下文行数（默认0）
              "maxResults": 50             // 最大结果数（默认100）
            }

            使用示例：
            - queryFile("test.md", '{"keyword":"TODO"}')
            - queryFile("log.txt", '{"regex":"\\\\d{4}-\\\\d{2}-\\\\d{2}","context":1}')
            - queryFile("code.js", '{"keyword":"function","exclude":"//"}')
            """)
    public String queryFile(
            @ToolParam(description = "文件路径") String filePath,
            @ToolParam(description = "查询选项（JSON格式，可选）") String options) {

        if (StrUtil.isBlank(filePath)) {
            throw new IllegalArgumentException("文件路径不能为空");
        }

        String fullPath = this.resolvePath(filePath);
        log.info("查询文件: {}, 选项: {}", fullPath, options);

        try {
            List<String> lines = FileUtil.readUtf8Lines(fullPath);
            QueryOpts opts = this.parseOptions(options);

            // 确定行范围
            int startLine = opts.startLine != null && opts.startLine > 0 ? opts.startLine : 1;
            int endLine = opts.endLine != null && opts.endLine <= lines.size() ? opts.endLine : lines.size();

            List<String> results = new ArrayList<>();
            int matchCount = 0;
            List<Integer> matchedLines = new ArrayList<>();

            // 查找匹配的行
            for (int i = startLine - 1; i < endLine; i++) {
                String line = lines.get(i);
                if (this.matchesFilter(line, opts)) {
                    matchedLines.add(i + 1);
                }
            }

            if (matchedLines.isEmpty()) {
                return "未找到匹配的内容";
            }

            // 构建结果（带上下文）
            int context = opts.context != null ? opts.context : 0;
            for (int i = 0; i < matchedLines.size() && matchCount < opts.maxResults; i++) {
                int lineNum = matchedLines.get(i);
                int contextStart = Math.max(startLine, lineNum - context);
                int contextEnd = Math.min(endLine, lineNum + context);

                // 添加上下文
                for (int ln = contextStart; ln <= contextEnd; ln++) {
                    String prefix = (ln == lineNum) ? ">>>" : "   ";
                    results.add(prefix + " " + ln + ": " + lines.get(ln - 1));
                    matchCount++;
                    if (matchCount >= opts.maxResults) {
                        break;
                    }
                }

                // 添加分隔线
                if (i < matchedLines.size() - 1 && matchCount < opts.maxResults) {
                    int nextLine = matchedLines.get(i + 1);
                    if (nextLine - lineNum > context * 2 + 1) {
                        results.add("   ...");
                    }
                }
            }

            results.add("\n共找到 " + matchedLines.size() + " 行匹配");
            return String.join("\n", results);

        } catch (Exception e) {
            log.error("查询文件失败: {}", fullPath, e);
            throw new RuntimeException("查询文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 写入文件。
     *
     * @param fileName 文件名
     * @param content  文件内容
     * @return 文件完整路径
     * @since 1.0.0-SNAPSHOT
     */
    @Tool(description = """
            将内容写入文件。
            如果父目录不存在会自动创建。
            文件将写入配置的存储根路径下。
            """)
    public String writeFile(
            @ToolParam(description = "文件名，如 plan.md 或 session-123/data.md") String fileName,
            @ToolParam(description = "文件内容") String content) {

        if (StrUtil.isBlank(fileName)) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String fullPath = this.getStoragePath() + fileName;
        log.info("写入文件: {}", fullPath);

        try {
            FileUtil.mkParentDirs(fullPath);
            FileUtil.writeString(content, fullPath, StandardCharsets.UTF_8);
            return fullPath;
        } catch (Exception e) {
            log.error("文件写入失败: {}", fullPath, e);
            throw new RuntimeException("文件写入失败: " + e.getMessage(), e);
        }
    }

    /**
     * 追加内容到文件。
     *
     * @param fileName 文件名
     * @param content  要追加的内容
     * @return 文件完整路径
     * @since 1.0.0-SNAPSHOT
     */
    @Tool(description = """
            在文件末尾追加内容。
            如果文件不存在则创建新文件。
            适用于日志文件、增量数据等场景。
            """)
    public String appendFile(
            @ToolParam(description = "文件名") String fileName,
            @ToolParam(description = "要追加的内容") String content) {

        if (StrUtil.isBlank(fileName)) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String fullPath = this.getStoragePath() + fileName;
        log.info("追加文件: {}", fullPath);

        try {
            FileUtil.mkParentDirs(fullPath);
            FileUtil.appendString(fullPath, content, StandardCharsets.UTF_8);
            return fullPath;
        } catch (Exception e) {
            log.error("文件追加失败: {}", fullPath, e);
            throw new RuntimeException("文件追加失败: " + e.getMessage(), e);
        }
    }

    /**
     * 在会话目录下写入文件。
     *
     * @param sessionId 会话ID
     * @param fileType  文件类型（plan/data/content）
     * @param content   文件内容
     * @return 文件完整路径
     * @since 1.0.0-SNAPSHOT
     */
    @Tool(description = """
            在会话目录下创建文件并写入内容。
            会话ID用于隔离不同会话的文件。
            推荐的文件类型：
            - plan: 规划文件
            - data: 数据文件
            - content: 内容文件
            """)
    public String writeFileInSession(
            @ToolParam(description = "会话ID") String sessionId,
            @ToolParam(description = "文件类型（plan/data/content）") String fileType,
            @ToolParam(description = "文件内容") String content) {

        if (StrUtil.isBlank(sessionId) || StrUtil.isBlank(fileType)) {
            throw new IllegalArgumentException("会话ID和文件类型不能为空");
        }

        String fullPath = this.getStoragePath() + sessionId + "/" + fileType + ".md";
        log.info("写入会话文件: {}", fullPath);

        try {
            FileUtil.mkdir(this.getStoragePath() + sessionId);
            FileUtil.writeString(content, fullPath, StandardCharsets.UTF_8);
            return fullPath;
        } catch (Exception e) {
            log.error("会话文件写入失败: {}", fullPath, e);
            throw new RuntimeException("会话文件写入失败: " + e.getMessage(), e);
        }
    }

    /**
     * 列出目录文件。
     *
     * @param directoryPath 目录路径
     * @param recursive     是否递归
     * @param extension     文件扩展名过滤
     * @return 文件列表
     * @since 1.0.0-SNAPSHOT
     */
    @Tool(description = """
            列出目录下的文件。

            参数：
            - directoryPath: 目录路径（为空则列出存储根目录）
            - recursive: 是否递归子目录（默认false）
            - extension: 文件扩展名过滤，如 ".md"（为空则返回所有文件）

            返回格式：文件名|类型|大小(字节)
            类型：FILE 或 DIRECTORY
            """)
    public List<String> listFiles(
            @ToolParam(description = "目录路径（可选）") String directoryPath,
            @ToolParam(description = "是否递归子目录（默认false）") Boolean recursive,
            @ToolParam(description = "文件扩展名过滤，如 .md（可选）") String extension) {

        String fullPath = StrUtil.isBlank(directoryPath) ? this.getStoragePath() : this.resolvePath(directoryPath);
        boolean isRecursive = recursive != null && recursive;
        log.info("列出目录: {}, 递归: {}, 扩展名: {}", fullPath, isRecursive, extension);

        try {
            File directory = new File(fullPath);
            if (!directory.exists() || !directory.isDirectory()) {
                throw new IllegalArgumentException("路径不存在或不是目录: " + directoryPath);
            }

            List<String> result = new ArrayList<>();
            if (isRecursive) {
                this.listFilesRecursive(directory, this.getStoragePath(), result, extension);
            } else {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        String type = file.isDirectory() ? "DIRECTORY" : "FILE";
                        String size = file.isDirectory() ? "-" : String.valueOf(file.length());
                        result.add(file.getName() + "|" + type + "|" + size);
                    }
                }
            }

            return result;
        } catch (Exception e) {
            log.error("列出目录失败: {}", fullPath, e);
            throw new RuntimeException("列出目录失败: " + e.getMessage(), e);
        }
    }


    /**
     * 删除文件或目录。
     *
     * @param path 文件或目录路径
     * @return 是否成功
     * @since 1.0.0-SNAPSHOT
     */
    @Tool(description = """
            删除文件或目录。
            如果是目录，会递归删除目录下的所有内容。
            删除操作不可恢复，请谨慎使用。
            """)
    public boolean deleteFile(
            @ToolParam(description = "文件或目录路径") String path) {

        if (StrUtil.isBlank(path)) {
            throw new IllegalArgumentException("路径不能为空");
        }

        String fullPath = this.resolvePath(path);
        log.info("删除: {}", fullPath);

        try {
            File file = new File(fullPath);
            if (!file.exists()) {
                return true;
            }
            return FileUtil.del(file);
        } catch (Exception e) {
            log.error("删除失败: {}", fullPath, e);
            throw new RuntimeException("删除失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查文件是否存在。
     *
     * @param filePath 文件路径
     * @return 是否存在
     * @since 1.0.0-SNAPSHOT
     */
    @Tool(description = """
            检查文件或目录是否存在。
            返回 true 表示存在，false 表示不存在。
            """)
    public boolean fileExists(
            @ToolParam(description = "文件路径") String filePath) {

        if (StrUtil.isBlank(filePath)) {
            return false;
        }

        String fullPath = this.resolvePath(filePath);
        return FileUtil.exist(fullPath);
    }

    /**
     * 创建目录。
     *
     * @param directoryPath 目录路径
     * @return 是否成功
     * @since 1.0.0-SNAPSHOT
     */
    @Tool(description = """
            创建目录。
            如果父目录不存在会自动创建。
            """)
    public boolean createDirectory(
            @ToolParam(description = "目录路径") String directoryPath) {

        if (StrUtil.isBlank(directoryPath)) {
            throw new IllegalArgumentException("目录路径不能为空");
        }

        String fullPath = this.resolvePath(directoryPath);
        log.info("创建目录: {}", fullPath);

        try {
            FileUtil.mkdir(fullPath);
            return true;
        } catch (Exception e) {
            log.error("创建目录失败: {}", fullPath, e);
            throw new RuntimeException("创建目录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析文件路径。
     *
     * @param path path
     * @return string
     * @since 1.0.0-SNAPSHOT
     */
    private String resolvePath(String path) {
        if (FileUtil.isAbsolutePath(path)) {
            return path;
        }
        return this.getStoragePath() + path;
    }

    /**
     * 获取存储路径。
     *
     * @return string
     * @since 1.0.0-SNAPSHOT
     */
    private String getStoragePath() {
        String path = this.applicationProperties.getStoragePath();
        if (StrUtil.isBlank(path)) {
            path = ApplicationProperties.DEFAULT_STORAGE_PATH;;
        }
        if (!path.endsWith("/")) {
            path += "/";
        }
        return path;
    }

    /**
     * 递归列出文件。
     *
     * @param directory directory
     * @param basePath base path
     * @param result result
     * @param extension extension
     * @since 1.0.0-SNAPSHOT
     */
    private void listFilesRecursive(File directory, String basePath, List<String> result, String extension) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                this.listFilesRecursive(file, basePath, result, extension);
            } else {
                if (StrUtil.isBlank(extension) || file.getName().endsWith(extension)) {
                    String relativePath = Paths.get(basePath).relativize(file.toPath()).toString();
                    String size = String.valueOf(file.length());
                    result.add(relativePath + "|FILE|" + size);
                }
            }
        }
    }

    /**
     * 解析查询选项。
     *
     * @param options options
     * @return query opts
     * @since 1.0.0-SNAPSHOT
     */
    private QueryOpts parseOptions(String options) {
        QueryOpts opts = new QueryOpts();
        opts.startLine = 1;
        opts.endLine = Integer.MAX_VALUE;
        opts.ignoreCase = true;
        opts.context = 0;
        opts.maxResults = 100;

        if (StrUtil.isBlank(options)) {
            return opts;
        }

        try {
            opts.startLine = this.parseInt(this.extractJson(options, "startLine"), 1);
            opts.endLine = this.parseInt(this.extractJson(options, "endLine"), Integer.MAX_VALUE);
            opts.keyword = this.extractJson(options, "keyword");
            opts.ignoreCase = this.parseBool(this.extractJson(options, "ignoreCase"), true);
            opts.exclude = this.extractJson(options, "exclude");
            opts.regex = this.extractJson(options, "regex");
            opts.context = this.parseInt(this.extractJson(options, "context"), 0);
            opts.maxResults = this.parseInt(this.extractJson(options, "maxResults"), 100);
        } catch (Exception e) {
            log.warn("解析查询选项失败，使用默认值: {}", options, e);
        }

        return opts;
    }

    /**
     * 提取JSON字段值。
     *
     * @param json json
     * @param field field
     * @return string
     * @since 1.0.0-SNAPSHOT
     */
    private String extractJson(String json, String field) {
        // 字符串值
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]*)\"");
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        // 数字值
        p = java.util.regex.Pattern.compile("\"" + field + "\"\\s*:\\s*(\\d+)");
        m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        // 布尔值
        p = java.util.regex.Pattern.compile("\"" + field + "\"\\s*:\\s*(true|false)");
        m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    /**
     * 解析整数。
     *
     * @param value value
     * @param defaultValue default value
     * @return integer
     * @since 1.0.0-SNAPSHOT
     */
    private Integer parseInt(String value, Integer defaultValue) {
        if (StrUtil.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 解析布尔值。
     *
     * @param value value
     * @param defaultValue default value
     * @return boolean
     * @since 1.0.0-SNAPSHOT
     */
    private Boolean parseBool(String value, Boolean defaultValue) {
        if (StrUtil.isBlank(value)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * 判断行是否匹配过滤条件。
     *
     * @param line line
     * @param opts opts
     * @return boolean
     * @since 1.0.0-SNAPSHOT
     */
    private boolean matchesFilter(String line, QueryOpts opts) {
        // 排除条件
        if (StrUtil.isNotBlank(opts.exclude)) {
            String target = opts.ignoreCase ? line.toLowerCase() : line;
            String exclude = opts.ignoreCase ? opts.exclude.toLowerCase() : opts.exclude;
            if (target.contains(exclude)) {
                return false;
            }
        }

        // 关键词匹配
        if (StrUtil.isNotBlank(opts.keyword)) {
            String target = opts.ignoreCase ? line.toLowerCase() : line;
            String keyword = opts.ignoreCase ? opts.keyword.toLowerCase() : opts.keyword;
            if (!target.contains(keyword)) {
                return false;
            }
        }

        // 正则匹配
        if (StrUtil.isNotBlank(opts.regex)) {
            if (!line.matches(opts.regex)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 查询选项。
     *
     * @author haijun
     * @email "mailto:iamxiaohaijun@gmail.com"
     * @date 2026/2/6 11:35
     * @version 1.0.0-SNAPSHOT
     * @since 1.0.0-SNAPSHOT
     */
    @Data
    private static class QueryOpts {
        /**
         * start line.
         */
        private Integer startLine;
        /**
         * end line.
         */
        private Integer endLine;
        /**
         * keyword.
         */
        private String keyword;
        /**
         * ignore case.
         */
        private Boolean ignoreCase;
        /**
         * exclude.
         */
        private String exclude;
        /**
         * regex.
         */
        private String regex;
        /**
         * context.
         */
        private Integer context;
        /**
         * max results.
         */
        private Integer maxResults;
    }
}
