package com.example.cinema.dto.content;

import com.example.cinema.type.ContentStatus;

public record ContentPatchRequest (
        String title,
        String description,
        ContentStatus status,

        Long posterAssetId,
        Long videoSourceAssetId,
        Long videoHlsMasterAssetId
) {}

