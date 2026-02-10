package io.github.hijun.agent.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.hijun.agent.common.enums.ChatMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 会话实体
 * <p>
 * 存储用户会话信息
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
@TableName("session")
public class Session extends BasePo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话标题
     */
    @TableField("title")
    private String title;

    /**
     * 模型ID
     */
    @TableField("model_id")
    private Long modelId;

    /**
     * 聊天模式
     */
    @TableField("chat_mode")
    private ChatMode chatMode;
}
