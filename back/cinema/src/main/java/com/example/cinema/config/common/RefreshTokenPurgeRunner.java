package com.example.cinema.config.common;

import com.example.cinema.repository.auth.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenPurgeRunner implements ApplicationRunner {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void run(ApplicationArguments args) {
        long count = refreshTokenRepository.count();
        if (count > 0) {
            refreshTokenRepository.deleteAll();
            log.warn("Refresh tokens cleared on startup. count={}", count);
        }
    }
}
