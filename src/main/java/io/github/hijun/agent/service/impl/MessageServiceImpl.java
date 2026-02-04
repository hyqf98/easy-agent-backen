package io.github.hijun.agent.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.hijun.agent.common.enums.MessageStatus;
import io.github.hijun.agent.converter.MessageConverter;
import io.github.hijun.agent.entity.dto.MessageDTO;
import io.github.hijun.agent.entity.po.Message;
import io.github.hijun.agent.entity.req.MessageForm;
import io.github.hijun.agent.mapper.MessageMapper;
import io.github.hijun.agent.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

/**
 * 消息服务实现
 * <p>
 * 提供消息的查询、保存、更新服务实现
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    private final MessageMapper messageMapper;

    /**
     * List By Session Id
     *
     * @param sessionId session id
     * @return list
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public List<MessageDTO> listBySessionId(Long sessionId) {
        Assert.notNull(sessionId, "会话ID不能为空");

        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<Message>()
                .eq(Message::getSessionId, sessionId)
                .orderByAsc(Message::getCreateTime);
        List<Message> list = super.list(wrapper);
        return MessageConverter.INSTANCE.toDto(list);
    }

    /**
     * Create
     *
     * @param form form
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public void create(MessageForm form) {
        Assert.notNull(form, "表单数据不能为空");

        Message entity = MessageConverter.INSTANCE.toPo(form);
        if (ObjectUtil.isNull(entity.getStatus())) {
            entity.setStatus(MessageStatus.COMPLETED);
        }
        super.save(entity);
    }
}
