package com.cfs.File_Services.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class Config implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // .allowedOriginPatterns("*") // old: wildcard origin combined with allowCredentials(true) is an insecure combination
                .allowedOrigins("http://localhost:3000")
                // .allowedMethods("GET,PUT,POST,DELETE,OPTION") // old: typo, "OPTION" doesn't match the HTTP verb "OPTIONS"
                .allowedMethods("GET,PUT,POST,DELETE,OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
