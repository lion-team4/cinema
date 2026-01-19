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

    @Scheduled(cron = "0 0 0,12 * * *", zone = "Asia/Seoul") // 자정/정오 하루 2회
    @Transactional
    public void deleteOldDraftContents() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);

        contentRepository.deleteOldDrafts(cutoff);
    }

}
