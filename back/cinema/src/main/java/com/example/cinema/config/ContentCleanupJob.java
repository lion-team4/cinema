package com.example.cinema.config;

import com.example.cinema.repository.content.ContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ContentCleanupJob {
    private final ContentRepository contentRepository;

    @Scheduled(fixedDelay = 10 * 60 * 1000) // 10분마다 (이전 실행 끝난 후 10분)
    @Transactional
    public void deleteOldDraftContents() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);

        contentRepository.deleteOldDrafts(cutoff);
    }

}
