package io.github.hijun.agent.common.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.lang.reflect.Method;


/**
 * Code Enum Serializer
 *
 * @author haijun
 * @email "mailto:haijun@email.com"
 * @date 2026/1/8 16:46
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 * @param <T> t
 */
public class CodeEnumSerializer<T extends Enum<?>> extends JsonSerializer<T> {

    /**
     * Serialize
     *
     * @param value value
     * @param gen gen
     * @param serializers serializers
     * @throws IOException
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        try {
            // 通过反射获取code字段的值
            Method getCodeMethod = value.getClass().getMethod("getCode");
            Object code = getCodeMethod.invoke(value);
            gen.writeObject(code);
        } catch (Exception e) {
            // 如果获取code失败，则回退到默认序列化
            gen.writeObject(value.name());
        }
    }
}
