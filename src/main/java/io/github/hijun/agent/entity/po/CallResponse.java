package io.github.hijun.agent.entity.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 智能体调用响应.
 *
 * @author hijun
 * @version 3.4.3
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/1/4 10:07
 * @since 3.4.3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallResponse {

    /**
     * 是否成功.
     */
    private Boolean success;

    /**
     * 响应消息.
     */
    private String message;

    /**
     * 响应数据.
     */
    private Object data;

    /**
     * 错误码.
     */
    private String errorCode;
}
