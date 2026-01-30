package io.github.hijun.agent.common.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import io.github.hijun.agent.common.enums.BaseEnum;

import java.io.IOException;

/**
 * Code Enum Deserializer
 * <p>
 * 支持实现了 BaseEnum 接口的枚举类型反序列化
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/1/8 16:45
 * @since 1.0.0-SNAPSHOT
 */
public class EnumDeserializer<T extends BaseEnum<?>> extends JsonDeserializer<T>
        implements ContextualDeserializer {

    /**
     * 目标类型
     */
    private JavaType targetType;

    /**
     * 无参构造函数（供 Jackson 初始化使用）
     *
     * @since 1.0.0-SNAPSHOT
     */
    public EnumDeserializer() {
    }

    /**
     * 构造函数
     *
     * @param targetType 目标类型
     * @since 1.0.0-SNAPSHOT
     */
    public EnumDeserializer(JavaType targetType) {
        this.targetType = targetType;
    }

    /**
     * 反序列化
     *
     * @param p    JsonParser
     * @param ctxt DeserializationContext
     * @return 枚举实例
     * @throws IOException IO异常
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value == null || value.isEmpty()) {
            return null;
        }

        Class<?> enumClass = this.targetType.getRawClass();

        // 检查是否实现了 BaseEnum 接口
        if (!BaseEnum.class.isAssignableFrom(enumClass)) {
            throw new IOException("Enum class " + enumClass.getName() + " must implement BaseEnum interface");
        }

        // 使用 BaseEnum.fromValue() 方法进行反序列化
        return BaseEnum.fromValue((Class<? extends T>) enumClass, value);
    }

    /**
     * 创建上下文反序列化器
     *
     * @param deserializationContext 反序列化上下文
     * @param beanProperty           Bean属性
     * @return JsonDeserializer
     * @throws JsonMappingException 映射异常
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext,
                                                BeanProperty beanProperty) throws JsonMappingException {
        if (beanProperty != null) {
            JavaType type = beanProperty.getType();
            return new EnumDeserializer(type);
        }
        return this;
    }
}
