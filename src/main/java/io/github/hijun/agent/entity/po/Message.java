package io.github.hijun.agent.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.hijun.agent.common.enums.MessageRole;
import io.github.hijun.agent.common.enums.MessageStatus;
import org.springframework.ai.chat.messages.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 消息实体
 * <p>
 * 存储会话消息内容
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("message")
public class Message extends BasePo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    @TableField("session_id")
    private Long sessionId;

    /**
     * 角色
     */
    @TableField("role")
    private MessageRole role;

    /**
     * 消息类型
     */
    @TableField("type")
    private MessageType type;

    /**
     * 消息内容
     * <p>
     * JSON格式存储复杂内容
     */
    @TableField("content")
    private String content;

    /**
     * 消息状态
     */
    @TableField("status")
    private MessageStatus status;
}
