package com.example.cinema.entity;

import com.example.cinema.entity.common.BaseEntity;
import com.example.cinema.type.ContentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "schedule_days", uniqueConstraints = {
        @UniqueConstraint(name = "uk_schedule_day_content_date", columnNames = {"content_id", "schedule_date"})
})
public class ScheduleDay extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_day_id")
    private Long scheduleDayId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;

    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private Boolean isLocked = false;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    public void updateLock(Boolean isLocked, LocalDateTime lockedAt) {
        this.isLocked = isLocked;
        this.lockedAt = lockedAt;

    }
}
