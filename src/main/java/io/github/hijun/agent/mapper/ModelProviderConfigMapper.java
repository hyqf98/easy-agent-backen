package io.github.hijun.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.hijun.agent.entity.po.ModelProviderConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 模型提供商配置 Mapper
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:haijun@email.com"
 * @date 2026/01/11
 * @since 3.4.3
 */
@Mapper
public interface ModelProviderConfigMapper extends BaseMapper<ModelProviderConfig> {
}
