package io.github.hijun.agent.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 测试模型连接结果 DTO
 * <p>
 * 用于返回模型连接测试的结果信息
 *
 * @author haijun
 * @version 3.4.3
 * @email "mailto:haijun@email.com"
 * @date 2026/01/11
 * @since 3.4.3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestModelResultDTO {

    /**
     * 测试是否成功
     * <p>
     * true 表示连接测试成功，false 表示失败
     */
    private Boolean success;

    /**
     * 结果消息
     * <p>
     * 测试结果的详细描述信息，成功时返回成功信息，失败时返回错误原因
     */
    private String message;

    /**
     * 连接延迟
     * <p>
     * 连接测试所消耗的时间（毫秒）
     */
    private Long latency;

    /**
     * 模型标识
     * <p>
     * 被测试的模型 ID
     */
    private String model;
}
