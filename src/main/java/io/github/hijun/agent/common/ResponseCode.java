package io.github.hijun.agent.common;

import lombok.Getter;

/**
 * 响应码枚举
 * <p>
 * 定义系统中所有的响应码和对应的响应消息
 *
 * @author haijun
 * @version 3.4.3
 * @date 2026/01/12
 */
@Getter
public enum ResponseCode {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 失败
     */
    ERROR(500, "操作失败"),

    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),

    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权"),

    /**
     * 禁止访问
     */
    FORBIDDEN(403, "禁止访问"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 方法不允许
     */
    METHOD_NOT_ALLOWED(405, "方法不允许"),

    /**
     * 请求超时
     */
    REQUEST_TIMEOUT(408, "请求超时"),

    /**
     * 请求实体过大
     */
    REQUEST_ENTITY_TOO_LARGE(413, "请求实体过大"),

    /**
     * 内部服务器错误
     */
    INTERNAL_SERVER_ERROR(500, "内部服务器错误"),

    /**
     * 服务不可用
     */
    SERVICE_UNAVAILABLE(503, "服务不可用"),

    /**
     * 业务异常
     */
    BUSINESS_ERROR(600, "业务异常"),

    /**
     * 模型调用失败
     */
    MODEL_CALL_ERROR(700, "模型调用失败"),

    /**
     * 配置错误
     */
    CONFIG_ERROR(800, "配置错误");

    /**
     * 响应码
     */
    private final Integer code;

    /**
     * 响应消息
     */
    private final String message;

    ResponseCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
