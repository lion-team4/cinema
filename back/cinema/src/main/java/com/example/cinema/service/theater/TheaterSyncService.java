package com.example.cinema.service.theater;

import com.example.cinema.dto.theater.PlaybackStateResponse;
import com.example.cinema.entity.ScheduleItem;
import com.example.cinema.repository.schedule.ScheduleItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TheaterSyncService {

    private final ScheduleItemRepository scheduleItemRepository;

    public PlaybackStateResponse getState(long scheduleId) {
        // 한국 시간 기준으로 통일
        long serverTimeMs = System.currentTimeMillis();

        ScheduleItem item = scheduleItemRepository.findById(scheduleId).orElse(null);

        return PlaybackStateResponse.from(item, serverTimeMs);
    }


}
