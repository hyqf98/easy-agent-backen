package io.github.hijun.agent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * FileTools Test
 *
 * @author hijun
 * @version 1.0.0
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/1/7 15:36
 * @since 1.0.0
 */
@SpringBootTest
class SimpleFileWriteToolTest {

    /**
     * file tools.
     */
    @Autowired
    private FileTools fileTools;

    /**
     * Test Write File
     *
     * @since 1.0.0
     */
    @Test
    void testWriteFile() {
        String result = fileTools.writeFile("test.md", "# Test Markdown\nThis is a test.");
        System.out.println("File written to: " + result);
    }

    /**
     * Test Write File In Session
     *
     * @since 1.0.0
     */
    @Test
    void testWriteFileInSession() {
        String sessionId = "test-session-" + System.currentTimeMillis();
        String result = fileTools.writeFileInSession(sessionId, "plan", "## Test Plan\nThis is a test plan.");
        System.out.println("File written to: " + result);
    }

    /**
     * Test Read File
     *
     * @since 1.0.0
     */
    @Test
    void testReadFile() {
        // 先写入文件
        String writeResult = fileTools.writeFile("test-read.md", "# Test Content\nContent to read.");

        // 读取文件
        String content = fileTools.readFile("test-read.md");
        System.out.println("File content: " + content);
    }
}
