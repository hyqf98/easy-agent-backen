package io.github.hijun.agent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.hijun.agent.entity.dto.SessionDTO;
import io.github.hijun.agent.entity.po.Session;
import io.github.hijun.agent.entity.req.SessionForm;
import io.github.hijun.agent.entity.req.SessionQuery;

import java.util.List;

/**
 * 会话服务接口
 * <p>
 * 提供会话的增删改查服务
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
public interface SessionService extends IService<Session> {

    /**
     * 根据ID查询会话
     *
     * @param id 主键ID
     * @return DTO
     * @since 1.0.0-SNAPSHOT
     */
    SessionDTO getById(Long id);

    /**
     * 查询会话列表
     *
     * @param query 查询条件
     * @return DTO 列表
     * @since 1.0.0-SNAPSHOT
     */
    List<SessionDTO> list(SessionQuery query);

    /**
     * 新增会话
     *
     * @param form 表单实体
     * @since 1.0.0-SNAPSHOT
     */
    void create(SessionForm form);

    /**
     * 修改会话
     *
     * @param form 表单实体
     * @since 1.0.0-SNAPSHOT
     */
    void update(SessionForm form);

    /**
     * 批量删除会话
     *
     * @param ids 主键ID列表
     * @since 1.0.0-SNAPSHOT
     */
    void removeByIds(List<Long> ids);

    /**
     * 清空会话消息
     *
     * @param id 会话ID
     * @since 1.0.0-SNAPSHOT
     */
    void clearMessages(Long id);
}
