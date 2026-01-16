package com.example.cinema.service.asset;

import com.example.cinema.entity.Content;
import com.example.cinema.entity.MediaAsset;
import com.example.cinema.entity.User;
import com.example.cinema.exception.BusinessException;
import com.example.cinema.exception.ErrorCode;
import com.example.cinema.repository.mediaAsset.MediaAssetRepository;
import com.example.cinema.type.AssetType;
import com.example.cinema.type.Visibility;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

@Service
@RequiredArgsConstructor
public class MediaAssetService {

    private final MediaAssetRepository mediaAssetRepository;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Transactional
    public MediaAsset createAsset(User owner,
                                  AssetType type,
                                  String objectKey,
                                  String contentType,
                                  Visibility visibility,
                                  HeadObjectResponse head) {

        MediaAsset asset = MediaAsset.builder()
                .owner(owner)
                .assetType(type)
                .bucket(bucket)
                .objectKey(objectKey)
                .contentType(contentType)
                .visibility(visibility)
                .sizeBytes(head == null ? null : head.contentLength())
                .durationMs(null)
                .build();

        return mediaAssetRepository.save(asset);
    }

    @Transactional
    public void attachToContent(Content content, AssetType type, MediaAsset asset) {
        switch (type) {
            case POSTER_IMAGE -> content.attachAssets(asset, null, null);
            case VIDEO_SOURCE -> content.attachAssets(null, asset, null);
            case VIDEO_HLS_MASTER -> content.attachAssets(null, null, asset);
            default -> throw new BusinessException("지원하지 않는 에셋 타입입니다: " + type, ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}