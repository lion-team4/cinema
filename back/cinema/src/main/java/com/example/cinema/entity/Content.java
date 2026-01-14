package com.example.cinema.entity;

import com.example.cinema.dto.content.ContentRequest;
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


    // 필수 값만 가져와서 영화등록
    public Content(User owner, String title, String description) {
        this.owner = owner;
        this.title = title;
        this.description = description;
        this.status = (status==null) ? ContentStatus.DRAFT : status;
    }

    public void updateInfo(String title, String description, ContentStatus status) {
        this.title = title;
        this.description = description;
        this.status = status;
    }

    // assets를 가져와서 추가 등록
    public void attachAssets(MediaAsset poster, MediaAsset videoSource, MediaAsset videoHlsMaster) {
        if (poster != null) this.poster = poster;
        if (videoSource != null) this.videoSource = videoSource;
        if (videoHlsMaster != null) this.videoHlsMaster = videoHlsMaster;
    }
}
