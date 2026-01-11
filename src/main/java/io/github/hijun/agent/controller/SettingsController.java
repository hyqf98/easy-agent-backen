package io.github.hijun.agent.controller;

import io.github.hijun.agent.entity.dto.ProviderInfoDTO;
import io.github.hijun.agent.entity.dto.SettingsConfigDTO;
import io.github.hijun.agent.entity.dto.TestModelResultDTO;
import io.github.hijun.agent.entity.req.SaveConfigRequest;
import io.github.hijun.agent.entity.req.TestModelRequest;
import io.github.hijun.agent.service.SettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 设置控制器
 * <p>
 * 提供系统设置相关的 REST API 接口
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:haijun@email.com"
 * @date 2026/01/11
 * @since 3.4.3
 */
@Slf4j
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    /**
     * 设置服务
     */
    private final SettingsService settingsService;

    /**
     * 获取所有配置
     * <p>
     * 查询并返回系统的所有配置信息，包括 MCP 服务器列表和模型提供商列表
     *
     * @return 系统配置信息 DTO
     */
    @GetMapping("/config")
    public SettingsConfigDTO getConfig() {
        return settingsService.getConfig();
    }

    /**
     * 保存配置
     * <p>
     * 保存 MCP 服务器配置和模型提供商配置到数据库
     *
     * @param request 配置保存请求，包含 MCP 服务器列表和模型提供商列表
     */
    @PostMapping("/config")
    public void saveConfig(@Valid @RequestBody SaveConfigRequest request) {
        settingsService.saveConfig(request);
    }

    /**
     * 获取支持的模型提供商列表
     * <p>
     * 返回系统支持的所有模型提供商信息，包括提供商名称、是否需要 API Key 等元数据
     *
     * @return 模型提供商信息列表
     */
    @GetMapping("/providers")
    public List<ProviderInfoDTO> getProviders() {
        return settingsService.getProviders();
    }

    /**
     * 测试模型连接
     * <p>
     * 测试指定模型的连接是否可用，返回测试结果和延迟信息
     *
     * @param request 测试请求，包含提供商类型、模型 ID、API Key 等信息
     * @return 测试结果 DTO，包含是否成功、消息和延迟时间
     */
    @PostMapping("/test-model")
    public TestModelResultDTO testModel(@Valid @RequestBody TestModelRequest request) {
        return settingsService.testModel(request);
    }
}
