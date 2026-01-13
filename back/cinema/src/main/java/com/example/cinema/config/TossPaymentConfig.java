package com.example.cinema.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.Base64;

/**
 * 토스 페이먼츠 API 통신을 위한 RestClient 설정
 */
@Configuration
public class TossPaymentConfig {

    @Value("${toss.secret-key}")
    private String secretKey;

    @Value("${toss.url}")
    private String baseUrl;

    @Bean
    public RestClient tossRestClient() {
        // Basic Auth Header: Base64(SecretKey + ":")
        String encodedKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Basic " + encodedKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
