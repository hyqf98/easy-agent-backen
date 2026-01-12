package io.github.hijun.agent.common.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import java.io.IOException;
import java.lang.reflect.Method;


/**
 * Code Enum Deserializer
 *
 * @param <T> t
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/1/8 16:45
 * @since 1.0.0-SNAPSHOT
 */
public class CodeEnumDeserializer<T extends Enum<?>> extends JsonDeserializer<T> implements ContextualDeserializer {

    /**
     * target type.
     */
    private JavaType targetType;

    /**
     * Code Enum Deserializer
     *
     * @since 1.0.0-SNAPSHOT
     */ // 1. 无参构造函数（供 Jackson 初始化使用）
    public CodeEnumDeserializer() {
    }

    /**
     * Code Enum Deserializer
     *
     * @param targetType target type
     * @since 1.0.0-SNAPSHOT
     */
    public CodeEnumDeserializer(JavaType targetType) {
        this.targetType = targetType;
    }

    /**
     * Deserialize
     *
     * @param p    p
     * @param ctxt ctxt
     * @return enum
     * @throws IOException
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        // 尝试多种方式获取枚举类
        Class<?> enumClass = this.targetType.getRawClass();
        try {
            // 获取枚举的所有值
            Enum<?>[] enumConstants = (Enum<?>[]) enumClass.getEnumConstants();

            if (enumConstants == null || enumConstants.length == 0) {
                throw new IOException("Enum class " + enumClass.getName() + " has no constants");
            }

            // 通过反射获取getCode方法
            Method getCodeMethod = enumClass.getMethod("getCode");

            // 遍历枚举，找到匹配code的枚举值
            for (Enum<?> enumConstant : enumConstants) {
                Object code = getCodeMethod.invoke(enumConstant);
                if (value.equals(String.valueOf(code))) {
                    return (T) enumConstant;
                }
            }
        } catch (Exception e) {
            throw new IOException("Failed to deserialize enum from code: " + value, e);
        }
        return null;
    }

    /**
     * Create Contextual
     *
     * @param deserializationContext deserialization context
     * @param beanProperty           bean property
     * @return json deserializer
     * @throws JsonMappingException
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext,
                                                BeanProperty beanProperty) throws JsonMappingException {
        // 如果 property 为 null，说明可能是在反序列化根对象，或者处于 List/Map 内部泛型等复杂情况
        if (beanProperty != null) {
            JavaType type = beanProperty.getType();
            // 返回一个新的实例，携带了字段类型信息
            return new CodeEnumDeserializer<>(type);
        }
        // 如果没有属性信息，直接返回当前实例或默认逻辑
        return this;
    }
}
