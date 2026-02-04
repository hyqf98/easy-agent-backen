package io.github.hijun.agent.controller;

import io.github.hijun.agent.common.validation.UpdateGroup;
import io.github.hijun.agent.entity.dto.SessionDTO;
import io.github.hijun.agent.entity.req.SessionForm;
import io.github.hijun.agent.entity.req.SessionQuery;
import io.github.hijun.agent.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.groups.Default;
import java.util.List;

/**
 * 会话管理控制器
 * <p>
 * 提供会话的增删改查接口，支持会话重命名、清空消息等操作
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
@Tag(name = "会话管理", description = "会话相关接口")
public class SessionController {

    /**
     * 会话服务
     */
    private final SessionService sessionService;

    /**
     * 根据ID查询会话
     *
     * @param id 主键ID
     * @return 会话DTO
     * @since 1.0.0-SNAPSHOT
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询会话", description = "根据主键ID查询会话详情")
    public SessionDTO getById(@PathVariable Long id) {
        return this.sessionService.getById(id);
    }

    /**
     * 查询会话列表
     *
     * @param query 查询条件
     * @return 会话DTO列表
     * @since 1.0.0-SNAPSHOT
     */
    @PostMapping("/list")
    @Operation(summary = "查询会话列表", description = "根据条件查询会话列表")
    public List<SessionDTO> list(@RequestBody SessionQuery query) {
        return this.sessionService.list(query);
    }

    /**
     * 新增会话
     *
     * @param form 表单实体
     * @since 1.0.0-SNAPSHOT
     */
    @PostMapping
    @Operation(summary = "新增会话", description = "新增会话")
    public void create(@Validated @RequestBody SessionForm form) {
        this.sessionService.create(form);
    }

    /**
     * 修改会话
     *
     * @param form 表单实体
     * @since 1.0.0-SNAPSHOT
     */
    @PutMapping
    @Operation(summary = "修改会话", description = "修改会话标题等信息")
    public void update(@Validated({UpdateGroup.class, Default.class}) @RequestBody SessionForm form) {
        this.sessionService.update(form);
    }

    /**
     * 批量删除会话
     *
     * @param ids 主键ID列表
     * @since 1.0.0-SNAPSHOT
     */
    @DeleteMapping("/remove")
    @Operation(summary = "批量删除会话", description = "根据ID列表批量删除会话")
    public void remove(@RequestBody List<Long> ids) {
        this.sessionService.removeByIds(ids);
    }

    /**
     * 清空会话消息
     *
     * @param id 会话ID
     * @since 1.0.0-SNAPSHOT
     */
    @DeleteMapping("/clear/{id}")
    @Operation(summary = "清空会话消息", description = "清空指定会话的所有消息")
    public void clearMessages(@PathVariable Long id) {
        this.sessionService.clearMessages(id);
    }
}
