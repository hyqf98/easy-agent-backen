package io.github.hijun.agent.converter;

import io.github.hijun.agent.entity.dto.McpConfigDTO;
import io.github.hijun.agent.entity.po.McpConfig;
import io.github.hijun.agent.entity.req.McpConfigForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

/**
 * MCP配置转换器
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Mapper
public interface McpConfigConverter extends BaseConverter<McpConfig, McpConfigDTO, McpConfigForm> {

    /**
     * INSTANCE
     */
    McpConfigConverter INSTANCE = Mappers.getMapper(McpConfigConverter.class);

    /**
     * PO 转 DTO（带协议类型描述映射）
     */
    @Mapping(target = "connectionTypeDesc", source = "connectionType", qualifiedByName = "connectionTypeToDesc")
    @Override
    McpConfigDTO toDto(McpConfig po);

    /**
     * 自定义转换方法：协议类型转描述
     */
    @Named("connectionTypeToDesc")
    default String connectionTypeToDesc(io.github.hijun.agent.common.enums.ConnectionType connectionType) {
        return connectionType == null ? null : connectionType.getDesc();
    }
}
