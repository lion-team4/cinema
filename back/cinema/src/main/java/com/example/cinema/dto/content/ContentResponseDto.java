package com.example.cinema.dto.content;

import com.example.cinema.entity.Content;

public record ContentResponseDto(
        String title,
        String description,
        String status,

        Long posterAssetId,
        Long videoSourceAssetId,
        Long videoHlsMasterAssetId
) {
    public static ContentResponseDto from(Content c) {
        return new ContentResponseDto(
                c.getTitle(),
                c.getDescription(),
                c.getStatus().name(),
                c.getPoster() == null ? null : c.getPoster().getAssetId(),
                c.getVideoSource() == null ? null : c.getVideoSource().getAssetId(),
                c.getVideoHlsMaster() == null ? null : c.getVideoHlsMaster().getAssetId()
        );
    }
}

