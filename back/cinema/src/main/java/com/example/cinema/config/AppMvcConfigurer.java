package com.example.cinema.config;

import com.example.cinema.type.SortField;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppMvcConfigurer implements WebMvcConfigurer {
    @Override
    public void addFormatters(org.springframework.format.FormatterRegistry registry) {
        registry.addConverter(String.class, SortField.class, SortField::from);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:3000",
                        "http://127.0.0.1:3000",
                        "http://13.124.18.8:3000",
                        "https://13.124.18.8:3000"
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
