package io.github.hijun.agent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.hijun.agent.common.enums.MessageStatus;
import io.github.hijun.agent.entity.dto.MessageDTO;
import io.github.hijun.agent.entity.po.Message;
import io.github.hijun.agent.entity.req.MessageForm;

import java.util.List;

/**
 * 消息服务接口
 * <p>
 * 提供消息的查询、保存、更新服务
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
public interface MessageService extends IService<Message> {

    /**
     * 获取会话的所有消息
     *
     * @param sessionId 会话ID
     * @return DTO 列表
     * @since 1.0.0-SNAPSHOT
     */
    List<MessageDTO> listBySessionId(Long sessionId);

    /**
     * 新增消息
     *
     * @param form 表单实体
     * @since 1.0.0-SNAPSHOT
     */
    void create(MessageForm form);
}
