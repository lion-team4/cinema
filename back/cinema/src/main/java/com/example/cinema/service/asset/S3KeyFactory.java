package com.example.cinema.service.asset;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class S3KeyFactory {

    public String posterKey(long contentId, String contentType, String fileName) {
        return "posters/" + contentId + "/poster" + imgExt(contentType, fileName);
    }

    public String videoSourceKey(long contentId) {
        return "uploads/" + contentId + "/source.mp4";
    }

    public String hlsMasterKey(long contentId) {
        return "hls/" + contentId + "/index.m3u8";
    }

    private String imgExt(String contentType, String fileName) {
        if (contentType != null) {
            String ct = contentType.toLowerCase(Locale.ROOT);
            if (ct.contains("png")) return ".png";
            if (ct.contains("jpeg") || ct.contains("jpg")) return ".jpg";
            if (ct.contains("webp")) return ".webp";
        }
        if (fileName != null) {
            String f = fileName.toLowerCase(Locale.ROOT);
            if (f.endsWith(".png")) return ".png";
            if (f.endsWith(".jpg") || f.endsWith(".jpeg")) return ".jpg";
            if (f.endsWith(".webp")) return ".webp";
        }
        return ".jpg";
    }
}
