package com.example.cinema.dto.content;

import com.example.cinema.entity.Content;

public record ContentResponseDto(
        Long contentId,
        String title,
        String description,
        String status,
        Long totalView,
        Long monthView,

        Long posterAssetId,
        Long videoSourceAssetId,
        Long videoHlsMasterAssetId
) {
    public static ContentResponseDto from(Content c) {
        return new ContentResponseDto(
                c.getContentId(),
                c.getTitle(),
                c.getDescription(),
                c.getStatus().name(),
                c.getTotalView(),
                c.getMonthView(),
                c.getPoster() == null ? null : c.getPoster().getAssetId(),
                c.getVideoSource() == null ? null : c.getVideoSource().getAssetId(),
                c.getVideoHlsMaster() == null ? null : c.getVideoHlsMaster().getAssetId()
        );
    }
}

