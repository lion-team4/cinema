package com.example.cinema.service.theater;

import com.example.cinema.dto.theater.PlaybackStateResponse;
import com.example.cinema.entity.ScheduleItem;
import com.example.cinema.repository.schedule.ScheduleItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class TheaterSyncService {

    private final ScheduleItemRepository scheduleItemRepository;
    private final Clock clock = Clock.systemUTC();

    public PlaybackStateResponse getState(long scheduleId) {
        long serverTimeMs = clock.millis();

        ScheduleItem item = scheduleItemRepository.findById(scheduleId).orElse(null);

        return PlaybackStateResponse.from(item, serverTimeMs);
    }
}
