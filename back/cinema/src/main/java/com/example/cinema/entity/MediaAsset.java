package com.example.cinema.entity;

import com.example.cinema.entity.common.BaseEntity;
import com.example.cinema.type.AssetType;
import com.example.cinema.type.Visibility;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "media_assets")
public class MediaAsset extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asset_id")
    private Long assetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false)
    private AssetType assetType;

    @Column(nullable = false)
    private String bucket; //s3 버켓 이름

    @Column(name = "object_key", nullable = false)
    private String objectKey;

    @Column(name = "content_type")
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "duration_ms")
    private Long durationMs;
}
