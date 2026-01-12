package io.github.hijun.agent.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.hijun.agent.common.enums.ModelProvider;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JSONS 工具类测试
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
class JSONSTest {

    /**
     * 测试简单对象序列化和反序列化
     */
    @Test
    void testSimpleObjectSerialization() {
        TestObject obj = new TestObject(1L, "test", ModelProvider.OPENAI);
        
        String json = JSONS.toJson(obj);
        assertNotNull(json);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"name\":\"test\""));
        assertTrue(json.contains("\"provider\":\"openai\"")); // 枚举应该以code形式序列化
        
        TestObject parsed = JSONS.parse(json, TestObject.class);
        assertEquals(obj.getId(), parsed.getId());
        assertEquals(obj.getName(), parsed.getName());
        assertEquals(obj.getProvider(), parsed.getProvider());
    }

    /**
     * 测试带泛型的集合反序列化
     */
    @Test
    void testGenericCollectionDeserialization() {
        List<TestObject> list = Arrays.asList(
                new TestObject(1L, "test1", ModelProvider.OPENAI),
                new TestObject(2L, "test2", ModelProvider.ANTHROPIC)
        );
        
        String json = JSONS.toJson(list);
        assertNotNull(json);
        
        List<TestObject> parsedList = JSONS.parse(json, new TypeReference<List<TestObject>>() {});
        assertEquals(2, parsedList.size());
        assertEquals("test1", parsedList.get(0).getName());
        assertEquals(ModelProvider.OPENAI, parsedList.get(0).getProvider());
        assertEquals("test2", parsedList.get(1).getName());
        assertEquals(ModelProvider.ANTHROPIC, parsedList.get(1).getProvider());
    }

    /**
     * 测试Map序列化和反序列化
     */
    @Test
    void testMapSerialization() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", 1L);
        map.put("name", "test");
        map.put("provider", ModelProvider.OPENAI.getCode());
        map.put("timestamp", LocalDateTime.now());
        
        String json = JSONS.toJson(map);
        assertNotNull(json);
        
        Map<String, Object> parsedMap = JSONS.parse(json, new TypeReference<Map<String, Object>>() {});
        assertEquals(1L, parsedMap.get("id"));
        assertEquals("test", parsedMap.get("name"));
        assertEquals(ModelProvider.OPENAI.getCode(), parsedMap.get("provider"));
    }

    /**
     * 测试嵌套对象序列化
     */
    @Test
    void testNestedObjectSerialization() {
        NestedTestObject nested = new NestedTestObject(
                new TestObject(1L, "nested", ModelProvider.AZURE_OPENAI),
                Arrays.asList(
                        new TestObject(2L, "child1", ModelProvider.OLLAMA),
                        new TestObject(3L, "child2", ModelProvider.OPENAI)
                )
        );
        
        String json = JSONS.toJson(nested);
        assertNotNull(json);
        
        NestedTestObject parsed = JSONS.parse(json, NestedTestObject.class);
        assertNotNull(parsed);
        assertEquals("nested", parsed.getParent().getName());
        assertEquals(2, parsed.getChildren().size());
        assertEquals("child1", parsed.getChildren().get(0).getName());
        assertEquals(ModelProvider.OLLAMA, parsed.getChildren().get(0).getProvider());
    }

    /**
     * 测试时间类型序列化
     */
    @Test
    void testDateTimeSerialization() {
        DateTimeTestObject obj = new DateTimeTestObject(LocalDateTime.now());
        
        String json = JSONS.toJson(obj);
        assertNotNull(json);
        
        DateTimeTestObject parsed = JSONS.parse(json, DateTimeTestObject.class);
        assertNotNull(parsed);
        assertNotNull(parsed.getTimestamp());
    }

    /**
     * 测试空值处理
     */
    @Test
    void testNullValueHandling() {
        TestObject obj = new TestObject(null, null, null);
        
        String json = JSONS.toJson(obj);
        assertNotNull(json);
        
        TestObject parsed = JSONS.parse(json, TestObject.class);
        assertNull(parsed.getId());
        assertNull(parsed.getName());
        assertNull(parsed.getProvider());
    }

    // 测试用的内部类
    static class TestObject {
        private Long id;
        private String name;
        private ModelProvider provider;

        public TestObject() {}

        public TestObject(Long id, String name, ModelProvider provider) {
            this.id = id;
            this.name = name;
            this.provider = provider;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public ModelProvider getProvider() { return provider; }
        public void setProvider(ModelProvider provider) { this.provider = provider; }
    }

    static class NestedTestObject {
        private TestObject parent;
        private List<TestObject> children;

        public NestedTestObject() {}

        public NestedTestObject(TestObject parent, List<TestObject> children) {
            this.parent = parent;
            this.children = children;
        }

        public TestObject getParent() { return parent; }
        public void setParent(TestObject parent) { this.parent = parent; }
        public List<TestObject> getChildren() { return children; }
        public void setChildren(List<TestObject> children) { this.children = children; }
    }

    static class DateTimeTestObject {
        private LocalDateTime timestamp;

        public DateTimeTestObject() {}

        public DateTimeTestObject(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}