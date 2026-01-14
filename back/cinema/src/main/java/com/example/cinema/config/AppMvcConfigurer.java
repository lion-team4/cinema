package com.example.cinema.config;

import com.example.cinema.type.SortField;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class AppMvcConfigurer implements WebMvcConfigurer {
    @Override
    public void addFormatters(org.springframework.format.FormatterRegistry registry) {
        registry.addConverter(String.class, SortField.class, SortField::from);
    }

}
