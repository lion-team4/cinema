package com.example.cinema.asset.transcode;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "transcode_jobs", indexes = {
        @Index(name = "idx_transcode_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class TranscodeJob {

    @Id
    private Long movieId;

    @Column(nullable = false, length = 500)
    private String sourceKey;  // uploads/.../source.mp4

    @Column(nullable = false, length = 500)
    private String hlsPrefix;  // hls/.../

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TranscodeStatus status;

    private Instant startedAt;
    private Instant finishedAt;

    @Column(length = 2000)
    private String errorMessage;

    public void markUploaded(String sourceKey, String hlsPrefix) {
        this.sourceKey = sourceKey;
        this.hlsPrefix = hlsPrefix;
        this.status = TranscodeStatus.UPLOADED;
        this.startedAt = null;
        this.finishedAt = null;
        this.errorMessage = null;
    }

    public void markTranscoding() {
        this.status = TranscodeStatus.TRANSCODING;
        this.startedAt = Instant.now();
        this.errorMessage = null;
    }

    public void markReady() {
        this.status = TranscodeStatus.READY;
        this.finishedAt = Instant.now();
    }

    public void markFailed(String msg) {
        this.status = TranscodeStatus.FAILED;
        this.finishedAt = Instant.now();
        this.errorMessage = msg;
    }
}

