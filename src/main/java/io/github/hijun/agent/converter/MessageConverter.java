package io.github.hijun.agent.converter;

import io.github.hijun.agent.entity.dto.MessageDTO;
import io.github.hijun.agent.entity.po.Message;
import io.github.hijun.agent.entity.req.MessageForm;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 消息转换器
 * <p>
 * 使用 MapStruct 进行 PO、Form、DTO 之间的转换
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Mapper
public interface MessageConverter extends BaseConverter<Message, MessageDTO, MessageForm> {

    /**
     * INSTANCE
     */
    MessageConverter INSTANCE = Mappers.getMapper(MessageConverter.class);
}
