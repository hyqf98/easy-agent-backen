package io.github.hijun.agent.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.hijun.agent.common.validation.UpdateGroup;
import io.github.hijun.agent.entity.dto.McpConfigDTO;
import io.github.hijun.agent.entity.dto.McpPromptDTO;
import io.github.hijun.agent.entity.dto.McpResourceDTO;
import io.github.hijun.agent.entity.dto.McpToolDTO;
import io.github.hijun.agent.entity.req.McpConfigForm;
import io.github.hijun.agent.entity.req.McpConfigQuery;
import io.github.hijun.agent.service.McpConfigService;
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
 * MCP配置控制器
 * <p>
 * 提供 MCP 配置的增删改查接口，支持连接测试和工具、资源、提示词查询
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/2/2 17:01
 * @since 1.0.0-SNAPSHOT
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/mcp")
@RequiredArgsConstructor
@Tag(name = "MCP配置管理", description = "MCP服务器配置相关接口")
public class McpConfigController {

    /**
     * MCP配置服务
     */
    private final McpConfigService mcpConfigService;

    /**
     * 根据ID查询MCP配置
     *
     * @param id 主键ID
     * @return MCP配置DTO
     * @since 1.0.0-SNAPSHOT
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询MCP配置", description = "根据主键ID查询MCP配置详情")
    public McpConfigDTO getById(@PathVariable Long id) {
        return this.mcpConfigService.getById(id);
    }

    /**
     * 分页查询MCP配置列表
     *
     * @param query 查询条件（包含分页参数）
     * @return 分页结果
     * @since 1.0.0-SNAPSHOT
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询MCP配置", description = "分页查询MCP配置列表，支持按连接类型、启用状态等条件筛选")
    public IPage<McpConfigDTO> page(@RequestBody McpConfigQuery query) {
        return this.mcpConfigService.page(query);
    }

    /**
     * 新增MCP配置
     *
     * @param form 表单实体
     * @since 1.0.0-SNAPSHOT
     */
    @PostMapping
    @Operation(summary = "新增MCP配置", description = "新增MCP配置")
    public void create(@Validated @RequestBody McpConfigForm form) {
        this.mcpConfigService.create(form);
    }

    /**
     * 修改MCP配置
     *
     * @param id   主键ID
     * @param form 表单实体
     * @since 1.0.0-SNAPSHOT
     */
    @PutMapping("/{id}")
    @Operation(summary = "修改MCP配置", description = "修改指定的MCP配置")
    public void update(@PathVariable Long id, @Validated({UpdateGroup.class, Default.class}) @RequestBody McpConfigForm form) {
        this.mcpConfigService.update(id, form);
    }

    /**
     * 批量删除MCP配置
     *
     * @param ids 主键ID列表
     * @since 1.0.0-SNAPSHOT
     */
    @DeleteMapping("/remove")
    @Operation(summary = "批量删除MCP配置", description = "根据ID列表批量删除MCP配置")
    public void remove(@RequestBody List<Long> ids) {
        this.mcpConfigService.removeByIds(ids);
    }

    /**
     * 测试MCP服务器连接
     *
     * @param id 主键ID
     * @since 1.0.0-SNAPSHOT
     */
    @PostMapping("/{id}/test")
    @Operation(summary = "测试MCP服务器连接", description = "测试指定MCP配置的连接是否可用")
    public void testConnection(@PathVariable Long id) {
        this.mcpConfigService.testConnection(id);
    }

    /**
     * 查询MCP服务器的工具列表
     *
     * @param id 主键ID
     * @return 工具列表
     * @since 1.0.0-SNAPSHOT
     */
    @GetMapping("/{id}/tools")
    @Operation(summary = "查询MCP服务器的工具列表", description = "查询指定MCP服务器提供的所有工具")
    public List<McpToolDTO> listTools(@PathVariable Long id) {
        return this.mcpConfigService.listTools(id);
    }

    /**
     * 查询MCP服务器的资源列表
     *
     * @param id 主键ID
     * @return 资源列表
     * @since 1.0.0-SNAPSHOT
     */
    @GetMapping("/{id}/resources")
    @Operation(summary = "查询MCP服务器的资源列表", description = "查询指定MCP服务器提供的所有资源")
    public List<McpResourceDTO> listResources(@PathVariable Long id) {
        return this.mcpConfigService.listResources(id);
    }

    /**
     * 查询MCP服务器的提示词列表
     *
     * @param id 主键ID
     * @return 提示词列表
     * @since 1.0.0-SNAPSHOT
     */
    @GetMapping("/{id}/prompts")
    @Operation(summary = "查询MCP服务器的提示词列表", description = "查询指定MCP服务器提供的所有提示词")
    public List<McpPromptDTO> listPrompts(@PathVariable Long id) {
        return this.mcpConfigService.listPrompts(id);
    }
}
