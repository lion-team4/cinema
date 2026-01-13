package com.example.cinema.dto;

public record ContentAssetAttachRequest (
        Long posterAssetId,
        Long videoSourceAssetId,
        Long videoHlsMasterAssetId
){}
