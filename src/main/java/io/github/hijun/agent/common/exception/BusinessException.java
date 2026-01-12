package io.github.hijun.agent.common.exception;

import io.github.hijun.agent.common.ResponseCode;
import lombok.Getter;

/**
 * 业务异常类
 * <p>
 * 用于处理业务逻辑中的异常情况
 *
 * @author haijun
 * @version 3.4.3
 * @date 2026/01/12
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @since 1.0.0-SNAPSHOT
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * serial version u i d.
     */
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * Business Exception
     *
     * @param message message
     * @since 1.0.0-SNAPSHOT
     */
    public BusinessException(String message) {
        super(message);
        this.code = ResponseCode.BUSINESS_ERROR.getCode();
        this.message = message;
    }

    /**
     * Business Exception
     *
     * @param code code
     * @param message message
     * @since 1.0.0-SNAPSHOT
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * Business Exception
     *
     * @param responseCode response code
     * @since 1.0.0-SNAPSHOT
     */
    public BusinessException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.code = responseCode.getCode();
        this.message = responseCode.getMessage();
    }

    /**
     * Business Exception
     *
     * @param responseCode response code
     * @param message message
     * @since 1.0.0-SNAPSHOT
     */
    public BusinessException(ResponseCode responseCode, String message) {
        super(message);
        this.code = responseCode.getCode();
        this.message = message;
    }
}
