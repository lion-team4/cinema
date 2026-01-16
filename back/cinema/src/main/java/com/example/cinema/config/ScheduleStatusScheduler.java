package com.example.cinema.config;

import com.example.cinema.repository.schedule.ScheduleItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ScheduleStatusScheduler {

    private final ScheduleItemRepository repo;

    @Scheduled(fixedDelay = 10_000) // 10초마다
    @Transactional
    public void tick() {
        LocalDateTime now = LocalDateTime.now();

        repo.endingToClosed(now.minusMinutes(10)); // endAt+10 <= now
        repo.playingToEnding(now);                 // endAt <= now
        repo.waitingToPlaying(now);                // startAt <= now
        repo.closedToWaiting(now, now.plusMinutes(10)); // openAt <= now
    }
}