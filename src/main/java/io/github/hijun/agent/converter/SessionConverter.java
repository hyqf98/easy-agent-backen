package io.github.hijun.agent.converter;

import io.github.hijun.agent.entity.dto.SessionDTO;
import io.github.hijun.agent.entity.po.Session;
import io.github.hijun.agent.entity.req.SessionForm;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 会话转换器
 * <p>
 * 使用 MapStruct 进行 PO、Form、DTO 之间的转换
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Mapper
public interface SessionConverter extends BaseConverter<Session, SessionDTO, SessionForm> {

    /**
     * INSTANCE
     */
    SessionConverter INSTANCE = Mappers.getMapper(SessionConverter.class);
}
