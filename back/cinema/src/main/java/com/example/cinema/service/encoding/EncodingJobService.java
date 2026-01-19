package com.example.cinema.service.encoding;

import com.example.cinema.entity.Content;
import com.example.cinema.entity.MediaAsset;
import com.example.cinema.entity.User;
import com.example.cinema.exception.BusinessException;
import com.example.cinema.exception.ErrorCode;
import com.example.cinema.infra.s3.S3ObjectService;
import com.example.cinema.repository.content.ContentRepository;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.service.asset.MediaAssetService;
import com.example.cinema.service.asset.S3KeyFactory;
import com.example.cinema.type.AssetType;
import com.example.cinema.type.EncodingStatus;
import com.example.cinema.type.Visibility;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
public class EncodingJobService {

    private static final Logger log = LoggerFactory.getLogger(EncodingJobService.class);

    private final Executor encodingExecutor;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final HlsTranscodeService hlsTranscodeService;
    private final MediaAssetService mediaAssetService;
    private final S3KeyFactory keyFactory;
    private final EncodingTxService encodingTxService;
    private final S3ObjectService s3ObjectService;

    @Value("${upload.verify.video_min_bytes}")
    private long minBytesVideo;

    /**
     * VIDEO_SOURCE complete 이후 호출.
     * Content 잠금 + 필요한 값 선추출로 Lazy 문제 제거.
     */
    @Transactional
    public String start(long contentId) {
        Content content = contentRepository.findByIdForUpdate(contentId)
                .orElseThrow(() -> new BusinessException("콘텐츠를 찾을 수 없습니다. ID: " + contentId, ErrorCode.CONTENT_NOT_FOUND));

        if (content.getVideoSource() == null) {
            throw new BusinessException("원본 비디오 파일이 연결되지 않았습니다. contentId=" + contentId, ErrorCode.INVALID_INPUT_VALUE);
        }
        if (content.getVideoHlsMaster() != null) {
            throw new BusinessException("이미 HLS 변환이 완료된 콘텐츠입니다. contentId=" + contentId, ErrorCode.INVALID_INPUT_VALUE);
        }
        if (content.getEncodingStatus() == EncodingStatus.ENCODING) {
            throw new BusinessException("현재 인코딩 진행 중입니다. contentId=" + contentId, ErrorCode.INVALID_INPUT_VALUE);
        }

        String sourceKey = content.getVideoSource().getObjectKey();
        Long ownerUserId = content.getOwner().getUserId();

        if (content.getEncodingStatus() == EncodingStatus.FAILED) {
            s3ObjectService.deletePrefix("hls/" + contentId + "/");
        }

        content.markEncoding();

        String jobId = "ENC-" + contentId + "-" + System.currentTimeMillis();
        log.info("Encoding accepted. jobId={} contentId={} sourceKey={}", jobId, contentId, sourceKey);

        encodingExecutor.execute(() -> runEncoding(jobId, contentId, ownerUserId, sourceKey));

        return jobId;
    }

    private void runEncoding(String jobId, long contentId, Long ownerUserId, String sourceKey) {
        try {
            String masterKey = keyFactory.hlsMasterKey(contentId);

            HlsTranscodeService.HlsTranscodeResult result = hlsTranscodeService.transcodeAndUpload(
                    contentId, sourceKey, masterKey, minBytesVideo
            );

            User owner = userRepository.findById(ownerUserId)
                    .orElseThrow(() -> new BusinessException("소유자 정보를 찾을 수 없습니다. ID: " + ownerUserId, ErrorCode.USER_NOT_FOUND));

            var head = s3ObjectService.assertReady(
                    masterKey, 1, "application/", 3, new long[]{300, 600, 1200}
            );
            MediaAsset hls = mediaAssetService.createAsset(
                    owner,
                    AssetType.VIDEO_HLS_MASTER,
                    masterKey,
                    "application/vnd.apple.mpegurl",
                    Visibility.PUBLIC,
                    head,
                    result.durationMs()
            );

            encodingTxService.linkHlsMaster(contentId, hls.getAssetId());
            encodingTxService.markReady(contentId);

            log.info("Encoding success. jobId={} contentId={} segCount={} durationMs={}",
                    jobId, contentId, result.segmentCount(), result.durationMs());

        } catch (Exception e) {
            encodingTxService.markFailed(contentId, e.getMessage());
            s3ObjectService.deletePrefix("hls/" + contentId + "/");
            log.error("Encoding failed. jobId={} contentId={} err={}", jobId, contentId, e.getMessage(), e);
        }
    }

}