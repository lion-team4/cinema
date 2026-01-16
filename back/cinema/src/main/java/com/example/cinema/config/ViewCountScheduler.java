package com.example.cinema.config;

import com.example.cinema.entity.Content;
import com.example.cinema.entity.WatchHistory;
import com.example.cinema.repository.watchHistory.WatchHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountScheduler {

    private final WatchHistoryRepository watchHistoryRepository;

 
    @Scheduled(fixedDelay = 5 * 60 * 1000) 
    @Transactional
    public void countViews() {
       
        LocalDateTime threshold = LocalDateTime.now().minusHours(1);
        List<WatchHistory> eligibleHistories = watchHistoryRepository.findEligibleForViewCount(threshold);

        if (eligibleHistories.isEmpty()) { return; }

        int countedViews = 0;

        for (WatchHistory history : eligibleHistories) {
            try {
                Content content = history.getScheduleItem().getContent();

                
                content.incrementView();
                history.markViewCounted();
                countedViews++;
            } catch (Exception e) {
                log.error("Failed to count view for watchId={}: {}", 
                        history.getWatchId(), e.getMessage());
            }
        }
        if (countedViews > 0) {
            log.info("ViewCountScheduler: {} views counted", countedViews);
        }
    }
}
