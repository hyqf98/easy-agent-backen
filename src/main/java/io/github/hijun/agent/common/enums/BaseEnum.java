package io.github.hijun.agent.common.enums;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.hijun.agent.common.serializer.EnumDeserializer;
import io.github.hijun.agent.common.serializer.EnumSerializer;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 基础枚举接口
 * <p>定义枚举的通用结构，包含 value（泛型）和 desc 字段</p>
 *
 * @param <T> value 的类型
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/1/30
 * @since 1.0.0-SNAPSHOT
 */
@JsonDeserialize(using = EnumDeserializer.class)
@JsonSerialize(using = EnumSerializer.class)
public interface BaseEnum<T> {

    /**
     * 获取枚举的 value 值
     *
     * @return value 值
     */
    T getValue();

    /**
     * 获取枚举的描述信息
     *
     * @return 描述信息
     */
    String getDesc();

    /**
     * 根据 value 值获取对应的枚举实例
     * <p>子类需要重写此方法以提供具体的枚举类型</p>
     *
     * @param value value 值
     * @param <E>   枚举类型
     * @return 对应的枚举实例，如果找不到则返回 null
     */
    static <E extends BaseEnum<?>> E fromValue(Class<E> enumType, Object value) {
        if (value == null) {
            return null;
        }
        Map<Object, E> valueMap = Arrays.stream(enumType.getEnumConstants())
                .collect(Collectors.toMap(e -> e.getValue(), Function.identity()));
        return valueMap.get(value);
    }
}
