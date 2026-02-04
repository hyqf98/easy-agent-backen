package io.github.hijun.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.hijun.agent.entity.po.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息Mapper
 * <p>
 * 继承 MyBatis Plus 的 BaseMapper，获取通用CRUD方法
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
