package com.example.cinema.dto.theater;

import com.example.cinema.entity.MediaAsset;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 재생 정보 응답 DTO
 * <p>
 * 용도:
 * - 영상 재생에 필요한 URL 및 메타데이터 반환 (GET /theaters/{id}/playback)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaybackInfoResponse {

    private Long assetId;

    /** CloudFront public URL (HLS m3u8 또는 mp4) */
    private String videoUrl;

    /** e.g. application/vnd.apple.mpegurl 또는 video/mp4 */
    private String contentType;

    /** 영상 길이 (밀리초, optional) */
    private Long durationMs;

    public static PlaybackInfoResponse from(MediaAsset media, String url) {
        return PlaybackInfoResponse.builder()
                .assetId(media.getAssetId())
                .videoUrl(url)
                .contentType(media.getContentType())
                .durationMs(media.getDurationMs())
                .build();
    }
}
