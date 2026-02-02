package io.github.hijun.agent.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.hijun.agent.entity.dto.LlmModelDTO;
import io.github.hijun.agent.entity.req.LlmModelForm;
import io.github.hijun.agent.entity.req.LlmModelQuery;
import io.github.hijun.agent.service.LlmModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 大语言模型配置控制器
 * <p>
 * 提供模型配置的增删改查接口，支持分页查询和默认模型获取
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/2/2 17:04
 */
@Slf4j
@RestController
@RequestMapping("/model")
@RequiredArgsConstructor
@Tag(name = "模型配置管理", description = "大语言模型配置相关接口")
public class LlmModelController {

    /**
     * 模型配置服务
     */
    private final LlmModelService llmModelService;

    /**
     * 根据ID查询模型配置
     *
     * @param id 主键ID
     * @return 模型配置DTO
     * @since 1.0.0-SNAPSHOT
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询模型配置", description = "根据主键ID查询模型配置详情")
    public LlmModelDTO getById(@PathVariable Long id) {
        return this.llmModelService.getById(id);
    }

    /**
     * 根据模型编码查询
     *
     * @param modelCode 模型编码
     * @return 模型配置DTO
     * @since 1.0.0-SNAPSHOT
     */
    @GetMapping("/code/{modelCode}")
    @Operation(summary = "根据模型编码查询", description = "根据模型编码查询模型配置")
    public LlmModelDTO getByModelCode(@PathVariable String modelCode) {
        return this.llmModelService.getByModelCode(modelCode);
    }

    /**
     * 分页查询模型配置列表
     *
     * @param query 查询条件（包含分页参数）
     * @return 分页结果
     * @since 1.0.0-SNAPSHOT
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询模型配置", description = "分页查询模型配置列表，支持按提供商、启用状态等条件筛选")
    public IPage<LlmModelDTO> page(@RequestBody LlmModelQuery query) {
        return this.llmModelService.page(query);
    }

    /**
     * 查询模型配置列表
     *
     * @param query 查询条件
     * @return 模型配置DTO列表
     * @since 1.0.0-SNAPSHOT
     */
    @PostMapping("/list")
    @Operation(summary = "查询模型配置列表", description = "根据条件查询模型配置列表")
    public List<LlmModelDTO> list(@RequestBody LlmModelQuery query) {
        return this.llmModelService.list(query);
    }

    /**
     * 保存或更新模型配置
     *
     * @param form 表单实体
     * @since 1.0.0-SNAPSHOT
     */
    @PostMapping("/save")
    @Operation(summary = "保存或更新模型配置", description = "新增或更新模型配置，根据ID判断是新增还是更新")
    public void save(@Validated @RequestBody LlmModelForm form) {
        this.llmModelService.saveOrUpdate(form);
    }

    /**
     * 批量删除模型配置
     *
     * @param ids 主键ID列表
     * @since 1.0.0-SNAPSHOT
     */
    @DeleteMapping("/remove")
    @Operation(summary = "批量删除模型配置", description = "根据ID列表批量删除模型配置")
    public void remove(@RequestBody List<Long> ids) {
        this.llmModelService.removeByIds(ids);
    }
}
