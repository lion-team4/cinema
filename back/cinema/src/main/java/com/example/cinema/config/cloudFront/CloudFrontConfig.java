package com.example.cinema.config.cloudFront;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CloudFrontProperties.class)
public class CloudFrontConfig {
}