package io.github.hijun.agent.controller;

import io.github.hijun.agent.entity.dto.MessageDTO;
import io.github.hijun.agent.entity.req.MessageForm;
import io.github.hijun.agent.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 消息管理控制器
 * <p>
 * 提供消息的查询、保存、更新接口
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
@Tag(name = "消息管理", description = "消息相关接口")
public class MessageController {

    /**
     * 消息服务
     */
    private final MessageService messageService;

    /**
     * 获取会话的所有消息
     *
     * @param sessionId 会话ID
     * @return 消息DTO列表
     * @since 1.0.0-SNAPSHOT
     */
    @GetMapping("/list/{sessionId}")
    @Operation(summary = "获取会话消息", description = "获取指定会话的所有消息")
    public List<MessageDTO> listBySessionId(@PathVariable Long sessionId) {
        return this.messageService.listBySessionId(sessionId);
    }

    /**
     * 新增消息
     *
     * @param form 表单实体
     * @since 1.0.0-SNAPSHOT
     */
    @PostMapping
    @Operation(summary = "新增消息", description = "新增消息")
    public void create(@Validated @RequestBody MessageForm form) {
        this.messageService.create(form);
    }

}
