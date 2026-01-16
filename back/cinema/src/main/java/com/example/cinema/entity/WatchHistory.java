package com.example.cinema.entity;

import com.example.cinema.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "watch_histories")
public class WatchHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "watch_id")
    private Long watchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_item_id", nullable = false)
    private ScheduleItem scheduleItem;

    @Column(name = "enter_at")
    private LocalDateTime enterAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;




    /**
     * 조회수 카운트 여부 (1시간 이상 시청 시 true)
     * - true: 이미 totalView, monthView에 반영됨
     * - false: 아직 반영 안 됨
     */
    @Setter
    @Column(name = "view_counted", nullable = false)
    @Builder.Default
    private Boolean viewCounted = false;

    public static WatchHistory create(User user, ScheduleItem scheduleItem) {
        return WatchHistory.builder()
                .user(user)
                .scheduleItem(scheduleItem)
                .viewCounted(false)
                .build();
    }

    public void leave() {
        this.leftAt = LocalDateTime.now();
    }

    public boolean hasLeft() {
        return this.leftAt != null;
    }

    /**
     * 조회수 카운트 완료 표시
     */
    public void markViewCounted() {
        this.viewCounted = true;
    }

    public void reEnter(){
        this.leftAt = null;
    }
    public void enter(){
        this.enterAt = LocalDateTime.now();
    }

    public boolean isDiffMoreThanTenMinutes() {
        if (enterAt == null || leftAt == null) {
            return false;
        }

        // 두 시간의 절대적인 차이를 구함
        Duration duration = Duration.between(enterAt, leftAt);
        long minutes = Math.abs(duration.toMinutes());

        return minutes >= 5;
    }

}
