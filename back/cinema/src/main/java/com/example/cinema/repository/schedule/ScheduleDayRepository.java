package com.example.cinema.repository.schedule;

import com.example.cinema.entity.ScheduleDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleDayRepository extends JpaRepository<ScheduleDay, Long> {
    Optional<ScheduleDay> findByContent_ContentIdAndScheduleDate(Long contentId, LocalDate scheduleDate);


}
