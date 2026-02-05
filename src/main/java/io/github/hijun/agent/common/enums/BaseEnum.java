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
     * @since 1.0.0-SNAPSHOT
     */
    T getValue();

    /**
     * 获取枚举的描述信息
     *
     * @return 描述信息
     * @since 1.0.0-SNAPSHOT
     */
    String getDesc();

    /**
     * 根据 value 值获取对应的枚举实例
     * <p>
     * 查找策略：
     * 1. 首先尝试通过枚举的 getValue() 方法匹配
     * 2. 如果找不到，尝试将 value 转换为大写后通过枚举名称（name）匹配
     *
     * @param enumType 枚举类型
     * @param value    value 值或枚举名称
     * @param <E>      枚举类型
     * @return 对应的枚举实例，如果找不到则返回 null
     * @since 1.0.0-SNAPSHOT
     */
    static <E extends BaseEnum<?>> E fromValue(Class<E> enumType, Object value) {
        if (value == null) {
            return null;
        }

        // 构建值映射
        Map<Object, E> valueMap = Arrays.stream(enumType.getEnumConstants())
                .collect(Collectors.toMap(e -> e.getValue(), Function.identity()));

        // 首先尝试通过 value 查找
        E result = valueMap.get(value);
        if (result != null) {
            return result;
        }

        // 如果通过 value 找不到，尝试通过枚举名称查找
        String valueStr = value.toString().toUpperCase().trim();
        return Arrays.stream(enumType.getEnumConstants())
                .filter(e -> ((Enum<?>) e).name().equalsIgnoreCase(valueStr))
                .findFirst()
                .orElse(null);
    }
}
