package io.github.hijun.agent.common.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.github.hijun.agent.common.enums.BaseEnum;

import java.io.IOException;

/**
 * Code Enum Serializer
 * <p>
 * 支持实现了 BaseEnum 接口的枚举类型序列化
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/1/8 16:46
 * @since 1.0.0-SNAPSHOT
 */
public class EnumSerializer<T extends BaseEnum<?>> extends JsonSerializer<T> {

    /**
     * 序列化
     * <p>
     * 将枚举序列化为其 value 值
     *
     * @param value       枚举值
     * @param gen         JsonGenerator
     * @param serializers SerializerProvider
     * @throws IOException IO异常
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        // 直接调用 getValue() 方法获取值并序列化
        Object enumValue = value.getValue();
        gen.writeObject(enumValue);
    }
}
