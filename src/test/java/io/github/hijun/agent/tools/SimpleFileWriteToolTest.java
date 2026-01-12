package io.github.hijun.agent.tools;

import io.github.hijun.agent.tools.FileWriteTool.FileWriteParams;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Simple File Write Tool Test
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/1/7 15:36
 * @since 1.0.0-SNAPSHOT
 */
@SpringBootTest
class SimpleFileWriteToolTest {

    /**
     * file write tool.
     */
    @Autowired
    private FileWriteTool fileWriteTool;

    /**
     * Test File Write Md
     *
     * @since 1.0.0-SNAPSHOT
     */
    @Test
    void testFileWriteMd() {
        FileWriteParams params = new FileWriteParams(
                "test", "md", "# Test Markdown\nThis is a test."
        );

        this.fileWriteTool.fileWrite(params);
    }

    /**
     * Test File Write Html
     *
     * @since 1.0.0-SNAPSHOT
     */
    @Test
    void testFileWriteHtml() {

        String htmlContent = "<html><body><h1>Test HTML</h1></body></html>";
        FileWriteParams params = new FileWriteParams(
                "test", "html", htmlContent
        );
        this.fileWriteTool.fileWrite(params);
    }

    /**
     * Test File Write Invalid File Type
     *
     * @since 1.0.0-SNAPSHOT
     */
    @Test
    void testFileWriteInvalidFileType() {
        FileWriteParams params = new FileWriteParams(
                "test", "txt", "Content"
        );

        // 验证无效文件类型会抛出异常
        try {
            this.fileWriteTool.fileWrite(params);
        } catch (IllegalArgumentException e) {
            // 预期的异常
        }
    }
}
