package com.example.cinema.service.encoding;

import com.example.cinema.entity.Content;
import com.example.cinema.entity.MediaAsset;
import com.example.cinema.entity.User;
import com.example.cinema.exception.BusinessException;
import com.example.cinema.exception.ErrorCode;
import com.example.cinema.repository.content.ContentRepository;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.service.asset.MediaAssetService;
import com.example.cinema.service.asset.S3KeyFactory;
import com.example.cinema.type.AssetType;
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

        String sourceKey = content.getVideoSource().getObjectKey();
        Long ownerUserId = content.getOwner().getUserId();

        String jobId = "ENC-" + contentId + "-" + System.currentTimeMillis();
        log.info("Encoding accepted. jobId={} contentId={} sourceKey={}", jobId, contentId, sourceKey);

        encodingExecutor.execute(() -> runEncoding(jobId, contentId, ownerUserId, sourceKey));

        return jobId;
    }

    private void runEncoding(String jobId, long contentId, Long ownerUserId, String sourceKey) {
        try {
            String masterKey = keyFactory.hlsMasterKey(contentId);

            int segCount = hlsTranscodeService.transcodeAndUpload(
                    contentId, sourceKey, masterKey, minBytesVideo
            );

            User owner = userRepository.findById(ownerUserId)
                    .orElseThrow(() -> new BusinessException("소유자 정보를 찾을 수 없습니다. ID: " + ownerUserId, ErrorCode.USER_NOT_FOUND));

            MediaAsset hls = mediaAssetService.createAsset(
                    owner,
                    AssetType.VIDEO_HLS_MASTER,
                    masterKey,
                    "application/vnd.apple.mpegurl",
                    Visibility.PUBLIC,
                    null
            );

            encodingTxService.linkHlsMaster(contentId, hls.getAssetId());

            log.info("Encoding success. jobId={} contentId={} segCount={}", jobId, contentId, segCount);

        } catch (Exception e) {
            log.error("Encoding failed. jobId={} contentId={} err={}", jobId, contentId, e.getMessage(), e);
        }
    }

}