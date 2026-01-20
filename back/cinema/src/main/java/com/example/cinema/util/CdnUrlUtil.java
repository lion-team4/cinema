package com.example.cinema.util;

public final class CdnUrlUtil {
    private CdnUrlUtil() {}

    public static String buildCdnUrl(String objectKey, String cfDomain) {
        if (objectKey == null || objectKey.isBlank()) return objectKey;
        if (objectKey.startsWith("https://https://")) return objectKey.replace("https://https://", "https://");
        if (objectKey.startsWith("http://http://")) return objectKey.replace("http://http://", "http://");
        if (objectKey.startsWith("http://") || objectKey.startsWith("https://")) return objectKey;
        if (cfDomain == null || cfDomain.isBlank()) return objectKey;
        return "https://" + cfDomain + "/" + objectKey;
    }
}
