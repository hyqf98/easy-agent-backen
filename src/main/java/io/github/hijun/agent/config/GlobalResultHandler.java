package io.github.hijun.agent.config;

import io.github.hijun.agent.common.Result;
import lombok.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Global Result Handler
 * <p>
 * 全局响应处理器，自动包装 Controller 返回值为 Result 对象
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/2/4 13:04
 * @since 1.0.0-SNAPSHOT
 */
@RestControllerAdvice
public class GlobalResultHandler implements ResponseBodyAdvice<Object> {

    /**
     * 判断是否需要包装返回值
     * <p>
     * 以下情况不包装：
     * 1. 返回值类型已经是 Result
     * 2. 返回值类型是 Resource（如文件下载）
     * 3. 返回值类型是 Reactor 类型（Flux/Mono，如 SSE 流式响应）
     *
     * @param returnType    返回值类型
     * @param converterType 转换器类型
     * @return true-需要包装，false-不需要包装
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public boolean supports(MethodParameter returnType,
                            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> parameterType = returnType.getParameterType();

        // 如果返回值类型已经是 Result，不需要包装
        if (Result.class.isAssignableFrom(parameterType)) {
            return false;
        }

        // 如果返回值类型是 Resource，不需要包装（文件下载等场景）
        if (Resource.class.isAssignableFrom(parameterType)) {
            return false;
        }

        // 如果返回值类型是 Reactor 类型（Flux/Mono），不需要包装（SSE 流式响应）
        if (Flux.class.isAssignableFrom(parameterType) ||
                Mono.class.isAssignableFrom(parameterType)) {
            return false;
        }

        return true;
    }

    /**
     * 包装返回值
     *
     * @param body                  原始返回值
     * @param returnType            返回值类型
     * @param selectedContentType   选择的内容类型
     * @param selectedConverterType 选择的转换器类型
     * @param request               请求
     * @param response              响应
     * @return 包装后的 Result 对象
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public Object beforeBodyWrite(Object body,
                                  @NonNull MethodParameter returnType,
                                  @NonNull MediaType selectedContentType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NonNull ServerHttpRequest request,
                                  @NonNull ServerHttpResponse response) {
        // 如果返回值已经是 Result，直接返回
        if (body instanceof Result) {
            return body;
        }

        // 如果返回值为 null，返回空的成功响应
        if (body == null) {
            return Result.success();
        }

        // 包装返回值为 Result
        return Result.success(body);
    }
}
