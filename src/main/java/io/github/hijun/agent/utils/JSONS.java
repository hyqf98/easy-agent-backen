package io.github.hijun.agent.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.hijun.agent.common.serializer.CodeEnumDeserializer;
import io.github.hijun.agent.common.serializer.CodeEnumSerializer;

/**
 * JSON 工具类，提供 ObjectMapper 单例和常用 JSON 操作方法
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/1/12 14:58
 */
public class JSONS {

    /**
     * ObjectMapper 单例实例
     */
    private static volatile ObjectMapper objectMapper;

    /**
     * 获取 ObjectMapper 单例实例
     *
     * @return ObjectMapper 实例
     * @since 1.0.0-SNAPSHOT
     */
    public static ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            synchronized (JSONS.class) {
                if (objectMapper == null) {
                    objectMapper = createObjectMapper();
                }
            }
        }
        return objectMapper;
    }

    /**
     * 创建并配置 ObjectMapper 实例
     *
     * @return 配置好的 ObjectMapper 实例
     * @since 1.0.0-SNAPSHOT
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 注册 Java 8 时间模块以支持 LocalDateTime、LocalDate 等类型
        mapper.registerModule(new JavaTimeModule());

        // 注册枚举序列化/反序列化处理器
        SimpleModule enumModule = new SimpleModule();
        enumModule.addSerializer(Enum.class, new CodeEnumSerializer<>());
        enumModule.addDeserializer(Enum.class, new CodeEnumDeserializer<>());
        mapper.registerModule(enumModule);

        // 配置序列化选项
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // 配置反序列化选项
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

        return mapper;
    }

    /**
     * 将对象转换为 JSON 字符串
     *
     * @param obj 要转换的对象
     * @return JSON 字符串
     * @since 1.0.0-SNAPSHOT
     */
    public static String toJson(Object obj) {
        try {
            return getObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    /**
     * 将 JSON 字符串解析为指定类型的对象
     *
     * @param json  JSON 字符串
     * @param clazz 目标类型
     * @param <T>   目标类型泛型
     * @return 解析后的对象
     * @since 1.0.0-SNAPSHOT
     */
    public static <T> T parse(String json, Class<T> clazz) {
        try {
            return getObjectMapper().readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON to object", e);
        }
    }

    /**
     * 将 JSON 字符串解析为指定类型的对象，支持泛型
     *
     * @param json          JSON 字符串
     * @param typeReference 目标类型引用
     * @param <T>           目标类型泛型
     * @return 解析后的对象
     * @since 1.0.0-SNAPSHOT
     */
    public static <T> T parse(String json, TypeReference<T> typeReference) {
        try {
            return getObjectMapper().readValue(json, typeReference);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON to object with TypeReference", e);
        }
    }
}
