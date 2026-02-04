package io.github.hijun.agent.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 请求日志过滤器
 * <p>
 * 记录所有 HTTP 请求的详细信息，用于调试
 *
 * @author haijun
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 * @email "mailto:iamxiaohaijun@gmail.com"
 * @date 2026/2/4 14:08
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    /**
     * Do Filter Internal
     *
     * @param request request
     * @param response response
     * @param filterChain filter chain
     * @throws ServletException
     * @throws IOException
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
        filterChain.doFilter(cachedRequest, response);
    }

    /**
     * 缓存请求体的包装类
     *
     * @author haijun
     * @email "mailto:iamxiaohaijun@gmail.com"
     * @date 2026/2/4 14:08
     * @version 1.0.0-SNAPSHOT
     * @since 1.0.0-SNAPSHOT
     */
    private static class CachedBodyHttpServletRequest extends ContentCachingRequestWrapper {
        /**
         * cached body.
         */
        private byte[] cachedBody;

        /**
         * Cached Body Http Servlet Request
         *
         * @param request request
         * @since 1.0.0-SNAPSHOT
         */
        public CachedBodyHttpServletRequest(HttpServletRequest request) {
            super(request);
        }

        /**
         * Get Content
         *
         * @return input stream
         * @since 1.0.0-SNAPSHOT
         */
        protected InputStream getContent() {
            if (this.cachedBody == null) {
                this.cachedBody = super.getContentAsByteArray();
            }
            return new ByteArrayInputStream(this.cachedBody);
        }

        /**
         * Get Cached Body
         *
         * @return string
         * @since 1.0.0-SNAPSHOT
         */
        public String getCachedBody() {
            if (this.cachedBody == null) {
                try {
                    this.cachedBody = super.getContentAsByteArray();
                } catch (Exception e) {
                    return null;
                }
            }
            return new String(this.cachedBody, StandardCharsets.UTF_8);
        }
    }
}
