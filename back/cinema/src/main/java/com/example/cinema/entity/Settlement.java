package com.example.cinema.entity;

import com.example.cinema.entity.common.BaseEntity;
import com.example.cinema.type.SettlementStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "settlements")
public class Settlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlement_id")
    private Long settlementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user_id")
    private User creator;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "total_views")
    private Long totalViews;

    private Long amount;

    @Enumerated(EnumType.STRING)
    private SettlementStatus status;

    @Column(name = "month_view")
    private Long monthView;
}
