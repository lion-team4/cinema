package com.example.cinema.infra.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class S3ObjectService {

    private final S3Client s3;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public HeadObjectResponse assertReady(String key, long minBytes, String expectedContentTypePrefix,
                                          int maxAttempts, long[] backoffMillis) {
        RuntimeException last = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                HeadObjectResponse head = s3.headObject(
                        HeadObjectRequest.builder().bucket(bucket).key(key).build()
                );

                long len = head.contentLength() == null ? 0 : head.contentLength();
                if (len < minBytes) {
                    throw new IllegalStateException("S3 object too small. key=" + key + ", len=" + len);
                }

                if (expectedContentTypePrefix != null && !expectedContentTypePrefix.isBlank()) {
                    String ct = head.contentType();
                    if (ct == null || !ct.toLowerCase().startsWith(expectedContentTypePrefix.toLowerCase())) {
                        throw new IllegalStateException("Unexpected content-type. key=" + key + ", ct=" + ct);
                    }
                }

                return head;

            } catch (S3Exception e) {
                last = new RuntimeException("S3 head failed attempt=" + attempt + " key=" + key
                        + " err=" + e.awsErrorDetails().errorMessage(), e);
            } catch (RuntimeException e) {
                last = e;
            }

            if (attempt < maxAttempts) {
                sleepQuiet(backoffMillis[Math.min(attempt - 1, backoffMillis.length - 1)]);
            }
        }

        throw last != null ? last : new IllegalStateException("S3 object not ready: " + key);
    }

    public void downloadToFile(String key, Path dest) throws Exception {
        Files.createDirectories(dest.getParent());
        s3.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build(), dest);
    }

    public void uploadFile(String key, Path file, String contentType, String cacheControl) throws Exception {
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .cacheControl(cacheControl)
                .build();

        s3.putObject(req, RequestBody.fromFile(file));
    }

    public void deletePrefix(String prefix) {
        String token = null;
        do {
            ListObjectsV2Response resp = s3.listObjectsV2(
                    ListObjectsV2Request.builder()
                            .bucket(bucket)
                            .prefix(prefix)
                            .continuationToken(token)
                            .build()
            );

            List<ObjectIdentifier> toDelete = new ArrayList<>();
            resp.contents().forEach(obj -> toDelete.add(
                    ObjectIdentifier.builder().key(obj.key()).build()
            ));

            if (!toDelete.isEmpty()) {
                s3.deleteObjects(DeleteObjectsRequest.builder()
                        .bucket(bucket)
                        .delete(Delete.builder().objects(toDelete).build())
                        .build());
            }

            token = resp.isTruncated() ? resp.nextContinuationToken() : null;
        } while (token != null);
    }

    private void sleepQuiet(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
