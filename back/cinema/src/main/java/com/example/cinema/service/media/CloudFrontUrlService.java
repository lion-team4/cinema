package com.example.cinema.service.media;

import com.example.cinema.config.cloudFront.CloudFrontProperties;
import org.springframework.stereotype.Service;

@Service
public class CloudFrontUrlService {

    private final CloudFrontProperties props;

    public CloudFrontUrlService(CloudFrontProperties props) {
        this.props = props;
    }

    public String toPublicUrl(String objectKey) {
        String domain = trimTrailingSlash(required(props.getDomain(), "cloudfront.domain is required"));
        String prefix = normalizePrefix(props.getOriginPathPrefix());
        String key = normalizeKey(objectKey);

        if (prefix.isEmpty()) {
            return domain + "/" + key;
        }
        return domain + prefix + "/" + key;
    }

    private static String required(String v, String msg) {
        if (v == null || v.isBlank()) throw new IllegalStateException(msg);
        return v;
    }

    private static String trimTrailingSlash(String v) {
        return v.endsWith("/") ? v.substring(0, v.length() - 1) : v;
    }

    private static String normalizePrefix(String prefix) {
        if (prefix == null) return "";
        String p = prefix.trim();
        if (p.isEmpty() || p.equals("/")) return "";
        if (!p.startsWith("/")) p = "/" + p;
        if (p.endsWith("/")) p = p.substring(0, p.length() - 1);
        return p;
    }

    private static String normalizeKey(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            throw new IllegalStateException("objectKey is empty");
        }
        String k = objectKey.trim();
        while (k.startsWith("/")) k = k.substring(1);
        return k;
    }
}