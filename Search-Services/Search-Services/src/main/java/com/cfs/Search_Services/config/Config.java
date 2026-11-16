package com.cfs.Search_Services.config;

import feign.Retryer;
import org.springframework.context.annotation.Bean;
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

    @Bean
    public Retryer feignRetryer(){
        return Retryer.NEVER_RETRY; // this is we have written because feign client not try to  likethe above line we are writting because feign client is register with the eureka so when the service fail then feign try to call the service  that is it working  so it basically interfering so by this line we are avoiding that
    }
}
