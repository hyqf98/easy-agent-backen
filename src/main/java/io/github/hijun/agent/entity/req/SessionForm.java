package io.github.hijun.agent.entity.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 会话表单
 * <p>
 * 用于新增和修改会话的请求表单
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "会话表单")
public class SessionForm extends BaseForm {

    /**
     * 会话标题
     */
    @Size(max = 200, message = "会话标题长度不能超过200")
    @Schema(description = "会话标题")
    private String title;

    /**
     * 模型ID
     */
    @NotNull(message = "模型ID不能为空")
    @Schema(description = "模型ID")
    private Long modelId;
}
