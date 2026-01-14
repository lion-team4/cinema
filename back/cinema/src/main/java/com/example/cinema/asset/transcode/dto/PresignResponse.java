package com.example.cinema.asset.transcode.dto;

import java.util.Map;

public record PresignResponse(
        String uploadUrl,
        String objectKey,
        Map<String, String> requiredHeaders
) {}

