package com.example.campussystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Application configuration class
 * Contains basic application configurations
 */
@Configuration
public class AppConfig implements WebMvcConfigurer {
    
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 确保路径匹配正确工作
        configurer.setUseTrailingSlashMatch(true);
    }
}