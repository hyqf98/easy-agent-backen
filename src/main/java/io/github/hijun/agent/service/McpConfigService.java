package io.github.hijun.agent.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.hijun.agent.entity.dto.McpConfigDTO;
import io.github.hijun.agent.entity.dto.McpPromptDTO;
import io.github.hijun.agent.entity.dto.McpResourceDTO;
import io.github.hijun.agent.entity.dto.McpToolDTO;
import io.github.hijun.agent.entity.po.McpConfig;
import io.github.hijun.agent.entity.req.McpConfigForm;
import io.github.hijun.agent.entity.req.McpConfigQuery;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;

/**
 * MCP配置服务接口
 * <p>
 * 提供 MCP 配置的增删改查服务，支持连接测试和工具、资源、提示词查询
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
public interface McpConfigService extends IService<McpConfig> {

    /**
     * 分页查询MCP配置
     *
     * @param query 查询条件
     * @return 分页结果
     * @since 1.0.0-SNAPSHOT
     */
    IPage<McpConfigDTO> page(McpConfigQuery query);

    /**
     * 根据ID查询MCP配置
     *
     * @param id 主键ID
     * @return DTO
     * @since 1.0.0-SNAPSHOT
     */
    McpConfigDTO getById(Long id);

    /**
     * 新增MCP配置
     *
     * @param form 表单实体
     * @return 是否成功
     * @since 1.0.0-SNAPSHOT
     */
    boolean create(McpConfigForm form);

    /**
     * 修改MCP配置
     *
     * @param form 表单实体
     * @return 是否成功
     * @since 1.0.0-SNAPSHOT
     */
    boolean update(McpConfigForm form);

    /**
     * 批量删除MCP配置
     *
     * @param ids 主键ID列表
     * @return 是否成功
     * @since 1.0.0-SNAPSHOT
     */
    boolean removeByIds(List<Long> ids);

    /**
     * 根据配置ID列表获取工具回调
     *
     * @param ids MCP配置ID列表
     * @return 工具回调列表
     * @since 1.0.0-SNAPSHOT
     */
    List<ToolCallback> getToolCallbacksByIds(List<Long> ids);

    /**
     * 测试MCP服务器连接
     *
     * @param id 主键ID
     * @since 1.0.0-SNAPSHOT
     */
    void testConnection(Long id);

    /**
     * 查询MCP服务器的工具列表
     *
     * @param id 主键ID
     * @return 工具列表
     * @since 1.0.0-SNAPSHOT
     */
    List<McpToolDTO> listTools(Long id);

    /**
     * 查询MCP服务器的资源列表
     *
     * @param id 主键ID
     * @return 资源列表
     * @since 1.0.0-SNAPSHOT
     */
    List<McpResourceDTO> listResources(Long id);

    /**
     * 查询MCP服务器的提示词列表
     *
     * @param id 主键ID
     * @return 提示词列表
     * @since 1.0.0-SNAPSHOT
     */
    List<McpPromptDTO> listPrompts(Long id);
}
