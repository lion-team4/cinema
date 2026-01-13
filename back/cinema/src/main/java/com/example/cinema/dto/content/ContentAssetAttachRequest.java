package com.example.cinema.dto.content;

public record ContentAssetAttachRequest (
        Long posterAssetId,
        Long videoSourceAssetId,
        Long videoHlsMasterAssetId
){}
