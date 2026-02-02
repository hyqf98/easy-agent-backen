package io.github.hijun.agent.converter;

import io.github.hijun.agent.common.enums.ModelProvider;
import io.github.hijun.agent.entity.dto.LlmModelDTO;
import io.github.hijun.agent.entity.po.LlmModel;
import io.github.hijun.agent.entity.req.LlmModelForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 大语言模型配置转换器
 * <p>
 * 使用 MapStruct 进行 PO、Form、DTO 之间的转换
 * <p>
 * 继承 BaseConverter 获取通用转换方法，只需定义特殊转换逻辑
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Mapper
public interface LlmModelConverter extends BaseConverter<LlmModel, LlmModelDTO, LlmModelForm> {

    /**
     * i n s t a n c e.
     */
    LlmModelConverter INSTANCE = Mappers.getMapper(LlmModelConverter.class);

    /**
     * PO 转 DTO
     * <p>
     * 将数据库实体转换为响应 DTO，同时转换提供商类型为描述
     *
     * @param po 数据库实体
     * @return DTO
     * @since 1.0.0-SNAPSHOT
     */
    @Mapping(target = "providerDesc", source = "providerType", qualifiedByName = "providerToDesc")
    @Override
    LlmModelDTO toDto(LlmModel po);

    /**
     * PO 列表转 DTO 列表
     * <p>
     * 批量转换数据库实体为响应 DTO，同时转换提供商类型为描述
     *
     * @param pos 数据库实体列表
     * @return DTO 列表
     * @since 1.0.0-SNAPSHOT
     */
    @Mapping(target = "providerDesc", source = "providerType", qualifiedByName = "providerToDesc")
    @Override
    List<LlmModelDTO> toDto(List<LlmModel> pos);

    /**
     * ModelProvider 转 描述
     * <p>
     * 将提供商枚举转换为其描述文本
     *
     * @param providerType 提供商类型
     * @return 描述
     * @since 1.0.0-SNAPSHOT
     */
    @Named("providerToDesc")
    default String providerToDesc(ModelProvider providerType) {
        if (providerType == null) {
            return null;
        }
        return providerType.getDesc();
    }
}
