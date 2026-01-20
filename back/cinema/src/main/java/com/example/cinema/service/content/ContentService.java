package com.example.cinema.service.content;


import com.example.cinema.dto.common.PageResponse;
import com.example.cinema.dto.content.ContentSearchRequest;
import com.example.cinema.dto.content.ContentSearchResponse;
import com.example.cinema.dto.content.*;
import com.example.cinema.entity.Content;
import com.example.cinema.entity.MediaAsset;
import com.example.cinema.entity.User;
import com.example.cinema.exception.BusinessException;
import com.example.cinema.exception.ErrorCode;
import com.example.cinema.repository.content.ContentRepository;
import com.example.cinema.repository.mediaAsset.MediaAssetRepository;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.service.media.CloudFrontUrlService;
import com.example.cinema.type.AssetType;
import com.example.cinema.type.ContentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.cinema.type.AssetType.*;

@Service
@RequiredArgsConstructor
public class ContentService {
    private final ContentRepository contentRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final UserRepository userRepository;
    private final CloudFrontUrlService cloudFrontUrlService;

    //1차 컨텐츠 등록
    @Transactional
    public ContentResponse createContent(ContentRequest contentRequest, User user) {
        User persistentUser = getPersistentUser(user);
        //유저 권한 확인
        if (!persistentUser.getSeller())
            throw new BusinessException("감독 등록 후에 이용가능합니다.", ErrorCode.ACCESS_DENIED);

        Content content = contentRepository.save(
                new Content(persistentUser, contentRequest.getTitle(), contentRequest.getDescription()));

        return ContentResponse.from(content);
    }

    //2차 컨텐츠 등록
    @Transactional
    public ContentResponse addAssetsContent(
                ContentAssetAttachRequest assetAttachRequest,
                User user,
                Long contentId) {
        User persistentUser = getPersistentUser(user);
        Content content = getContent(contentId);
        validateOwner(persistentUser, content);

        //MediaAsset 을 dto에서 추출 후 컨텐츠에 저장
        MediaAsset poster = getAssetOrThrow(
                assetAttachRequest.getPosterAssetId(), POSTER_IMAGE, "등록되지 않은 포스터입니다.");
        MediaAsset videoSourceAsset = getAssetOrThrow(
                assetAttachRequest.getVideoSourceAssetId(), VIDEO_SOURCE, "등록되지 않은 비디오입니다.");
        MediaAsset videoHlsMasterAssetId = getAssetOrThrow(
                assetAttachRequest.getVideoHlsMasterAssetId(),VIDEO_HLS_MASTER,"등록되지 않은 비디오입니다.");

        content.attachAssets(poster, videoSourceAsset, videoHlsMasterAssetId);

        return ContentResponse.from(content);
    }

    //수정폼 조회
    @Transactional(readOnly = true)
    public ContentEditResponse getEditContent(User user, Long contentId) {
        User persistentUser = getPersistentUser(user);
        Content content = getContent(contentId);
        validateOwner(persistentUser, content);

        return ContentEditResponse.from(content);
    }

    //수정
    @Transactional
    public ContentEditResponse updateContent(User user,
                                                Long contentId,
                                                ContentUpdateRequest updateRequest) {
        User persistentUser = getPersistentUser(user);
        Content content = getContent(contentId);
        validateOwner(persistentUser, content);

        if (content.getStatus() == ContentStatus.PUBLISHED)
            throw new BusinessException("상영신청이 완료된 컨텐츠는 수정할 수 없습니다.", ErrorCode.INVALID_INPUT_VALUE);

//        updateRequest -> mediaAssets 추출 후 조회
        MediaAsset poster = getAssetOrThrow(
                updateRequest.getPosterAssetId(), POSTER_IMAGE, "등록되지 않은 포스터입니다.");

        MediaAsset videoSourceAsset = getAssetOrThrow(
                updateRequest.getVideoSourceAssetId(), VIDEO_SOURCE, "등록되지 않은 비디오입니다.");

        MediaAsset videoHlsMasterAssetId = getAssetOrThrow(
                updateRequest.getVideoHlsMasterAssetId(),VIDEO_HLS_MASTER,"등록되지 않은 비디오입니다.");

       //기본 정보 수정 및 애셋 수정
        content.updateInfo(updateRequest.getTitle(), updateRequest.getDescription(), updateRequest.getStatus());
        content.attachAssets(poster, videoSourceAsset, videoHlsMasterAssetId);


        return ContentEditResponse.from(content);
    }

    @Transactional(readOnly = true)
    public ContentEncodingStatusResponse getEncodingStatus(User user, Long contentId) {
        User persistentUser = getPersistentUser(user);
        Content content = getContent(contentId);
        validateOwner(persistentUser, content);
        return ContentEncodingStatusResponse.from(content);
    }


    public void deleteContent(User user, Long contentId) {
        User persistentUser = getPersistentUser(user);
        Content content = getContent(contentId);
        validateOwner(persistentUser, content);

        contentRepository.deleteById(contentId);
    }




    private void validateAssetType(MediaAsset asset, AssetType expected) {
        if (asset.getAssetType() != expected) {
            throw new BusinessException(
                    "자산 타입이 올바르지 않습니다. expected=" + expected + ", actual=" + asset.getAssetType(), ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private Content getContent(Long contentId) {
        return contentRepository.findById(contentId)
                .orElseThrow(()-> new BusinessException("해당 영화가 존재하지 않습니다.", ErrorCode.CONTENT_NOT_FOUND));
    }

    private User getPersistentUser(User user) {
        if (user == null || user.getUserId() == null) {
            throw new BusinessException("사용자 정보를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND);
        }
        return userRepository.findById(user.getUserId())
                .orElseThrow(() -> new BusinessException("사용자 정보를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND));
    }

    private void validateOwner(User user, Content content) {
        if(!user.getUserId().equals(content.getOwner().getUserId()))
            throw new BusinessException("접근 권한이 없습니다.", ErrorCode.ACCESS_DENIED);
    }
    private MediaAsset getAssetOrThrow(Long assetId, String notFoundMsg) {
        if (assetId == null) {
            throw new BusinessException("assetId는 null일 수 없습니다.", ErrorCode.INVALID_INPUT_VALUE);
        }
        return mediaAssetRepository.findById(assetId)
                .orElseThrow(() -> new BusinessException(notFoundMsg, ErrorCode.ENTITY_NOT_FOUND));
    }

    private MediaAsset getAssetOrThrow(Long assetId, AssetType expectedType, String notFoundMsg) {
        MediaAsset asset = getAssetOrThrow(assetId, notFoundMsg);
        validateAssetType(asset, expectedType);
        return asset;
    }

    public PageResponse<ContentSearchResponse> search(ContentSearchRequest request) {
        var page = contentRepository.searchContent(request);

        return PageResponse.from(page.map(content -> {
            String posterUrl = null;
            if (content.getPoster() != null && content.getPoster().getObjectKey() != null) {
                posterUrl = cloudFrontUrlService.toPublicUrl(content.getPoster().getObjectKey());
            }
            return ContentSearchResponse.from(content, posterUrl);
        }));
    }

    @Transactional(readOnly = true)
    public Content getContentDetail(Long contentId) {
        Content content = getContent(contentId);
        if (content.getStatus() != ContentStatus.PUBLISHED) {
            throw new BusinessException("공개되지 않은 콘텐츠입니다.", ErrorCode.ACCESS_DENIED);
        }
        return content;
    }
}
