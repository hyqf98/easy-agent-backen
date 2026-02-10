package io.github.hijun.agent.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.hijun.agent.common.enums.ChatMode;
import io.github.hijun.agent.converter.SessionConverter;
import io.github.hijun.agent.entity.dto.SessionDTO;
import io.github.hijun.agent.entity.po.Session;
import io.github.hijun.agent.entity.req.SessionForm;
import io.github.hijun.agent.entity.req.SessionQuery;
import io.github.hijun.agent.mapper.MessageMapper;
import io.github.hijun.agent.mapper.SessionMapper;
import io.github.hijun.agent.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

/**
 * 会话服务实现
 * <p>
 * 提供会话的增删改查服务实现
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class SessionServiceImpl extends ServiceImpl<SessionMapper, Session> implements SessionService {

    private final MessageMapper messageMapper;

    /**
     * Get By Id
     *
     * @param id id
     * @return session d t o
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public SessionDTO getById(Long id) {
        Session entity = super.getById(id);
        if (entity == null) {
            return null;
        }
        SessionDTO dto = SessionConverter.INSTANCE.toDto(entity);
        dto.setMessageCount(this.getMessageCount(id));
        return dto;
    }

    /**
     * List
     *
     * @param query query
     * @return list
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public List<SessionDTO> list(SessionQuery query) {
        LambdaQueryWrapper<Session> wrapper = this.buildQueryWrapper(query);
        List<Session> list = super.list(wrapper);
        return list.stream()
                .map(po -> {
                    SessionDTO dto = SessionConverter.INSTANCE.toDto(po);
                    dto.setMessageCount(this.getMessageCount(po.getId()));
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Create
     *
     * @param form form
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public void create(SessionForm form) {
        Assert.notNull(form, "表单数据不能为空");

        Session entity = SessionConverter.INSTANCE.toPo(form);
        entity.setTitle("新对话");
        // 设置默认聊天模式为智能问答
        if (entity.getChatMode() == null) {
            entity.setChatMode(ChatMode.CHAT);
        }
        super.save(entity);
    }

    /**
     * Update
     *
     * @param form form
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public void update(SessionForm form) {
        Assert.notNull(form, "表单数据不能为空");
        Assert.notNull(form.getId(), "ID不能为空");

        Session entity = SessionConverter.INSTANCE.toPo(form);
        super.updateById(entity);
    }

    /**
     * Remove By Ids
     *
     * @param ids ids
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public void removeByIds(List<Long> ids) {
        Assert.notEmpty(ids, "删除ID列表不能为空");

        // 逻辑删除会话
        super.removeByIds(ids);

        // 逻辑删除该会话的所有消息
        ids.forEach(id -> {
            messageMapper.update(null,
                    new LambdaUpdateWrapper<io.github.hijun.agent.entity.po.Message>()
                            .eq(io.github.hijun.agent.entity.po.Message::getSessionId, id)
                            .set(io.github.hijun.agent.entity.po.Message::getDeleted, 1)
            );
        });
    }

    /**
     * Clear Messages
     *
     * @param id id
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public void clearMessages(Long id) {
        Assert.notNull(id, "会话ID不能为空");

        messageMapper.update(null,
                new LambdaUpdateWrapper<io.github.hijun.agent.entity.po.Message>()
                        .eq(io.github.hijun.agent.entity.po.Message::getSessionId, id)
                        .set(io.github.hijun.agent.entity.po.Message::getDeleted, 1)
        );
    }

    /**
     * Get Message Count
     *
     * @param sessionId session id
     * @return long
     * @since 1.0.0-SNAPSHOT
     */
    private Long getMessageCount(Long sessionId) {
        return messageMapper.selectCount(
                new LambdaQueryWrapper<io.github.hijun.agent.entity.po.Message>()
                        .eq(io.github.hijun.agent.entity.po.Message::getSessionId, sessionId)
        );
    }

    /**
     * Build Query Wrapper
     *
     * @param query query
     * @return lambda query wrapper
     * @since 1.0.0-SNAPSHOT
     */
    private LambdaQueryWrapper<Session> buildQueryWrapper(SessionQuery query) {
        if (ObjectUtil.isNull(query)) {
            return new LambdaQueryWrapper<>();
        }

        LambdaQueryWrapper<Session> wrapper = new LambdaQueryWrapper<Session>()
                .eq(query.getModelId() != null, Session::getModelId, query.getModelId())
                .orderByDesc(Session::getCreateTime);

        Optional.ofNullable(query.getTitle())
                .filter(StrUtil::isNotBlank)
                .ifPresent(title -> wrapper.like(Session::getTitle, title));

        return wrapper;
    }
}
