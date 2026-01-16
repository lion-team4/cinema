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
    private final UserRepository userRepository;
    private final MediaAssetRepository mediaAssetRepository;

    //1차 컨텐츠 등록
    @Transactional
    public ContentResponse createContent(ContentRequest contentRequest, String email) {

        User user = getUser(email);

        //유저 권한 확인
        if (!user.getSeller())
            throw new BusinessException("감독 등록 후에 이용가능합니다.", ErrorCode.ACCESS_DENIED);

        Content content = contentRepository.save(
                new Content(user, contentRequest.getTitle(), contentRequest.getDescription()));

        return ContentResponse.from(content);
    }

    //2차 컨텐츠 등록
    @Transactional
    public ContentResponse addAssetsContent(
                ContentAssetAttachRequest assetAttachRequest,
                String email,
                Long contentId) {

        User user = getUser(email);
        Content content = getContent(contentId);
        validateOwner(user, content);

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
    public ContentEditResponse getEditContent(String email, Long contentId) {

        User user = getUser(email);
        Content content = getContent(contentId);
        validateOwner(user, content);

        return ContentEditResponse.from(content);
    }

    //수정
    @Transactional
    public ContentEditResponse updateContent(String email,
                                                Long contentId,
                                                ContentUpdateRequest updateRequest) {
        User user = getUser(email);
        Content content = getContent(contentId);
        validateOwner(user, content);

        if (content.getStatus() == ContentStatus.PUBLISHED)
            throw new BusinessException("상영신청이 완료된 컨텐츠는 수정할 수 없습니다.", ErrorCode.INVALID_INPUT_VALUE);

        //updateRequest -> mediaAssets 추출 후 조회
//        MediaAsset poster = getAssetOrThrow(
//                updateRequest.getPosterAssetId(), POSTER_IMAGE, "등록되지 않은 포스터입니다.");
//
//        MediaAsset videoSourceAsset = getAssetOrThrow(
//                updateRequest.getVideoSourceAssetId(), VIDEO_SOURCE, "등록되지 않은 비디오입니다.");
//
//        MediaAsset videoHlsMasterAssetId = getAssetOrThrow(
//                updateRequest.getVideoHlsMasterAssetId(),VIDEO_HLS_MASTER,"등록되지 않은 비디오입니다.");
//
//        //기본 정보 수정 및 애셋 수정
        content.updateInfo(updateRequest.getTitle(), updateRequest.getDescription(), updateRequest.getStatus());
//        content.attachAssets(poster, videoSourceAsset, videoHlsMasterAssetId);


        return ContentEditResponse.from(content);
    }


    public void deleteContent(String email, Long contentId) {
        User user = getUser(email);
        Content content = getContent(contentId);
        validateOwner(user, content);

        contentRepository.deleteById(contentId);
    }




    private void validateAssetType(MediaAsset asset, AssetType expected) {
        if (asset.getAssetType() != expected) {
            throw new BusinessException(
                    "자산 타입이 올바르지 않습니다. expected=" + expected + ", actual=" + asset.getAssetType(), ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(()-> new BusinessException("해당 유저가 존재하지 않습니다.", ErrorCode.USER_NOT_FOUND));
    }

    private Content getContent(Long contentId) {
        return contentRepository.findById(contentId)
                .orElseThrow(()-> new BusinessException("해당 영화가 존재하지 않습니다.", ErrorCode.CONTENT_NOT_FOUND));
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

        return PageResponse.from(page.map(ContentSearchResponse::from));
    }
}