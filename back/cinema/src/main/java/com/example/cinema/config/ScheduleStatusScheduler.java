package com.example.cinema.config;

import com.example.cinema.repository.schedule.ScheduleItemRepository;
import com.example.cinema.service.theater.TheaterStateBroadcaster;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

import static com.example.cinema.type.ScheduleStatus.*;


@Component
@RequiredArgsConstructor
public class ScheduleStatusScheduler {

    private final ScheduleItemRepository repo;
    private final TheaterStateBroadcaster broadcaster; // SimpMessagingTemplate 쓰는 서비스

    @Scheduled(fixedDelay = 10_000)
    @Transactional
    public void tick() {
        LocalDateTime now = LocalDateTime.now();

        // 오픈 로직은 유지(방송 필요 없으면 그대로)
        repo.closedToWaiting(now, now.plusMinutes(10));

        // 1) WAITING -> PLAYING
        var toPlaying = repo.findIdsWaitingToPlaying(now);
        if (!toPlaying.isEmpty()) {
            repo.updateStatusByIds(PLAYING, toPlaying);
            toPlaying.forEach(broadcaster::broadcastState);
        }

        // 2) PLAYING -> ENDING
        var toEnding = repo.findIdsPlayingToEnding(now);
        if (!toEnding.isEmpty()) {
            repo.updateStatusByIds(ENDING, toEnding);
            toEnding.forEach(broadcaster::broadcastState);
        }

        // 3) ENDING -> CLOSED
        var toClosed = repo.findIdsEndingToClosed(now.minusMinutes(10));
        if (!toClosed.isEmpty()) {
            repo.updateStatusByIds(CLOSED, toClosed);
            toClosed.forEach(broadcaster::broadcastState);
        }
    }
}