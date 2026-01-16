package com.example.cinema.repository.schedule.custom;

import com.example.cinema.dto.schedule.ScheduleSearchRequest;
import com.example.cinema.entity.ScheduleItem;
import org.springframework.data.domain.Page;

public interface ScheduleItemRepositoryCustom {
    Page<ScheduleItem> search(ScheduleSearchRequest request);
}
