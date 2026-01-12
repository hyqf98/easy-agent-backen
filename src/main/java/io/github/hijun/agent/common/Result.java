package io.github.hijun.agent.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应实体
 * <p>
 * 所有接口返回值的统一封装
 *
 * @author haijun
 * @version 3.4.3
 * @date 2026/01/12
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * Result
     *
     * @since 1.0.0-SNAPSHOT
     */
    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage(), null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResponseCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error() {
        return new Result<>(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage(), null);
    }

    /**
     * 失败响应（自定义消息）
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(ResponseCode.ERROR.getCode(), message, null);
    }

    /**
     * 失败响应（自定义码和消息）
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 失败响应（使用响应码枚举）
     */
    public static <T> Result<T> error(ResponseCode responseCode) {
        return new Result<>(responseCode.getCode(), responseCode.getMessage(), null);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return ResponseCode.SUCCESS.getCode().equals(this.code);
    }
}
