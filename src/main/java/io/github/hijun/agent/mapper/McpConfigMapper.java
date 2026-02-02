package io.github.hijun.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.hijun.agent.entity.po.McpConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * MCP配置Mapper
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Mapper
public interface McpConfigMapper extends BaseMapper<McpConfig> {
}
