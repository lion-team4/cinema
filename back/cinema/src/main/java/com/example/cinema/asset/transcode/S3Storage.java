package com.example.cinema.asset.transcode;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class S3Storage {

    private final S3Client s3;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public void downloadToFile(String key, Path dest) throws IOException {
        Files.createDirectories(dest.getParent());
        GetObjectRequest req = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        try (ResponseInputStream<GetObjectResponse> response = s3.getObject(req)) {
            Files.copy(response, dest);
        }
    }

    public void uploadFileWithHeaders(Path file, String key) throws IOException {
        String name = file.getFileName().toString().toLowerCase();
        HeaderSpec spec = headerSpec(name);

        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(spec.contentType())
                .cacheControl(spec.cacheControl())
                .build();

        s3.putObject(put, RequestBody.fromFile(file));
    }

    private HeaderSpec headerSpec(String filename) {
        if (filename.endsWith(".m3u8")) {
            return new HeaderSpec(
                    "application/vnd.apple.mpegurl",
                    "public, max-age=60, must-revalidate"
            );
        }
        if (filename.endsWith(".ts")) {
            return new HeaderSpec(
                    "video/mp2t",
                    "public, max-age=31536000, immutable"
            );
        }
        // fallback
        return new HeaderSpec("application/octet-stream", "public, max-age=60");
    }

    private record HeaderSpec(String contentType, String cacheControl) {}
}

