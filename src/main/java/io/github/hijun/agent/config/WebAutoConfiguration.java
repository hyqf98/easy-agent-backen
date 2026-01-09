package io.github.hijun.agent.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.hijun.agent.common.serializer.CodeEnumDeserializer;
import io.github.hijun.agent.common.serializer.CodeEnumSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置
 *
 * @author haijun
 * @date 2025-12-24
 * @email "mailto:haijun@email.com"
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0-SNAPSHOT
 */
@Configuration
public class WebAutoConfiguration implements WebMvcConfigurer {

    /**
     * 配置跨域
     *
     * @param registry 跨域注册器
     * @since 1.0.0-SNAPSHOT
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedHeaders("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * Jackson Customizer
     *
     * @return jackson2 object mapper builder customizer
     * @since 1.0.0-SNAPSHOT
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            module.addSerializer(Enum.class, new CodeEnumSerializer<>());
            module.addDeserializer(Enum.class, new CodeEnumDeserializer());
            builder.modules(module);
        };
    }
}
