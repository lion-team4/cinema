package com.example.cinema.asset.transcode;

import com.example.cinema.asset.transcode.dto.PresignRequest;
import com.example.cinema.asset.transcode.dto.PresignResponse;
import com.example.cinema.asset.transcode.dto.UploadCompleteRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/uploads")
public class UploadController {

    private final S3Presigner presigner;
    private final TranscodeService transcodeService;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @PostMapping("/presign")
    public PresignResponse presign(@Valid @RequestBody PresignRequest req) {
        // MVP: 규칙 고정. (원하면 uploads/{movieId}/source.mp4로 바꾸면 됨)
        String objectKey = "uploads/test/movie" + req.movieId() + "/" + req.filename();

        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(req.contentType())
                .build();

        PutObjectPresignRequest presignReq = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(put)
                .build();

        String url = presigner.presignPutObject(presignReq).url().toString();

        return new PresignResponse(
                url,
                objectKey,
                Map.of("Content-Type", req.contentType())
        );
    }

    @PostMapping("/complete")
    public ResponseEntity<?> complete(@Valid @RequestBody UploadCompleteRequest req) {
        boolean triggered = transcodeService.requestTranscode(req.movieId(), req.sourceKey());
        return ResponseEntity.accepted().body(Map.of(
                "movieId", req.movieId(),
                "triggered", triggered
        ));
    }
}

