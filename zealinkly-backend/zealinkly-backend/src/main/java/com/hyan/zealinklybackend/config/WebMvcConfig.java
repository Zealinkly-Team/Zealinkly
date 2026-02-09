package com.hyan.zealinklybackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Web MVC 配置
 * 确保所有响应使用 UTF-8 编码
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 扩展消息转换器，确保使用 UTF-8 编码
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof StringHttpMessageConverter) {
                StringHttpMessageConverter stringConverter = (StringHttpMessageConverter) converter;
                stringConverter.setDefaultCharset(StandardCharsets.UTF_8);
            } else if (converter instanceof MappingJackson2HttpMessageConverter) {
                MappingJackson2HttpMessageConverter jsonConverter = (MappingJackson2HttpMessageConverter) converter;
                jsonConverter.setDefaultCharset(StandardCharsets.UTF_8);
            }
        }
    }
}
