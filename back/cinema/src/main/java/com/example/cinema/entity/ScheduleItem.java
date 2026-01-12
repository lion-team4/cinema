package com.example.cinema.entity;

import com.example.cinema.entity.common.BaseEntity;
import com.example.cinema.type.ScheduleStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "schedule_items")
public class ScheduleItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_item_id")
    private Long scheduleItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_day_id", nullable = false)
    private ScheduleDay scheduleDay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleStatus status;
}
