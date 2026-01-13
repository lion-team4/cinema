package com.example.cinema.entity;

import com.example.cinema.entity.common.BaseEntity;
import com.example.cinema.type.ContentStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "contents")
public class Content extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "content_id")
    private Long contentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poster_asset_id")
    private MediaAsset poster;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_source_asset_id")
    private MediaAsset videoSource;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_hls_master_asset_id")
    private MediaAsset videoHlsMaster;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentStatus status;

    @Column(name = "total_view")
    @Builder.Default
    private Long totalView = 0L;

    @Column(name = "month_view")
    @Builder.Default
    private Long monthView = 0L;



}
