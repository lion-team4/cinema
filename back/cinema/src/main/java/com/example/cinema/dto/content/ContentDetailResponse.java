package com.example.cinema.dto.content;

import com.example.cinema.entity.Content;
import com.example.cinema.entity.MediaAsset;
import com.example.cinema.type.ContentStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ContentDetailResponse {
    private Long contentId;
    private String title;
    private String description;
    private ContentStatus status;
    private String ownerNickname;
    private String posterUrl;
    private String videoUrl;
    private Long durationMs;
    private List<String> tags;

    public static ContentDetailResponse from(Content content, String cfDomain) {
        MediaAsset poster = content.getPoster();
        MediaAsset hls = content.getVideoHlsMaster();

        String posterUrl = poster == null ? null : "https://" + cfDomain + "/" + poster.getObjectKey();
        String videoUrl = hls == null ? null : "https://" + cfDomain + "/" + hls.getObjectKey();
        Long durationMs = hls == null ? null : hls.getDurationMs();

        List<String> tags = content.getTagMaps().stream()
                .map(tagMap -> tagMap.getTag().getName())
                .toList();

        return ContentDetailResponse.builder()
                .contentId(content.getContentId())
                .title(content.getTitle())
                .description(content.getDescription())
                .status(content.getStatus())
                .ownerNickname(content.getOwner().getNickname())
                .posterUrl(posterUrl)
                .videoUrl(videoUrl)
                .durationMs(durationMs)
                .tags(tags)
                .build();
    }
}
