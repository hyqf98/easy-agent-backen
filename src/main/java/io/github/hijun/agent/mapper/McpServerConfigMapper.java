package io.github.hijun.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.hijun.agent.entity.po.McpServerConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * MCP 服务器配置 Mapper
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:haijun@email.com"
 * @date 2026/01/11
 * @since 3.4.3
 */
@Mapper
public interface McpServerConfigMapper extends BaseMapper<McpServerConfig> {
}
