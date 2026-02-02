package io.github.hijun.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.hijun.agent.entity.po.LlmModel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 大语言模型配置 Mapper
 * <p>
 * 提供模型配置的数据库操作接口
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Mapper
public interface LlmModelMapper extends BaseMapper<LlmModel> {
}
