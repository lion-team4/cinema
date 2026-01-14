package com.example.cinema.service.content;


import com.example.cinema.dto.content.*;
import com.example.cinema.entity.Content;
import com.example.cinema.entity.MediaAsset;
import com.example.cinema.entity.User;
import com.example.cinema.repository.MediaAssetRepository;
import com.example.cinema.repository.content.ContentRepository;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.type.AssetType;
import com.example.cinema.type.ContentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.example.cinema.type.AssetType.*;

@Service
@RequiredArgsConstructor
public class ContentService {
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final MediaAssetRepository mediaAssetRepository;

    //1차 컨텐츠 등록
    @Transactional
    public ContentResponseDto createContent(ContentRequestDto requestDto, String email) {

        User user = getUser(email);

        //유저 권한 확인
        if (!user.getSeller())
            throw new AccessDeniedException("감독 등록 후에 이용가능합니다.");

        Content content = contentRepository.save(
                new Content(user, requestDto.title(), requestDto.description()));

        return ContentResponseDto.from(content);
    }

    //2차 컨텐츠 등록
    @Transactional
    public ContentResponseDto addAssetsContent(
                ContentAssetAttachRequest assetDto,
                String email,
                Long contentId) {

        User user = getUser(email);
        Content content = getContent(contentId);
        validateOwner(user, content);

        //MediaAsset 을 dto에서 추출 후 컨텐츠에 저장
        MediaAsset poster = getAssetOrThrow(
                assetDto.posterAssetId(), POSTER_IMAGE, "등록되지 않은 포스터입니다.");
        MediaAsset videoSourceAsset = getAssetOrThrow(
                assetDto.videoSourceAssetId(), VIDEO_SOURCE, "등록되지 않은 비디오입니다.");
        MediaAsset videoHlsMasterAssetId = getAssetOrThrow(
                assetDto.videoHlsMasterAssetId(),VIDEO_HLS_MASTER,"등록되지 않은 비디오입니다.");

        content.attachAssets(poster, videoSourceAsset, videoHlsMasterAssetId);

        return ContentResponseDto.from(content);
    }

    //수정폼 조회
    @Transactional(readOnly = true)
    public ContentEditResponseDto getEditContent(String email, Long contentId) {

        User user = getUser(email);
        Content content = getContent(contentId);
        validateOwner(user, content);

        return ContentEditResponseDto.from(content);
    }

    //수정
    @Transactional
    public ContentEditResponseDto updateContent(String email,
                                                Long contentId,
                                                ContentUpdateRequestDto updateRequestDto) {
        User user = getUser(email);
        Content content = getContent(contentId);
        validateOwner(user, content);

        if (content.getStatus() == ContentStatus.PUBLISHED)
            throw new AccessDeniedException("상영신청이 완료된 컨텐츠는 수정할 수 없습니다.");

        //updateRequestDto -> mediaAssets 추출 후 조회
        MediaAsset poster = getAssetOrThrow(
                updateRequestDto.posterAssetId(), POSTER_IMAGE, "등록되지 않은 포스터입니다.");

        MediaAsset videoSourceAsset = getAssetOrThrow(
                updateRequestDto.videoSourceAssetId(), VIDEO_SOURCE, "등록되지 않은 비디오입니다.");

        MediaAsset videoHlsMasterAssetId = getAssetOrThrow(
                updateRequestDto.videoHlsMasterAssetId(),VIDEO_HLS_MASTER,"등록되지 않은 비디오입니다.");

        //기본 정보 수정 및 애셋 수정
        content.updateInfo(updateRequestDto.title(), updateRequestDto.description(), updateRequestDto.status());
        content.attachAssets(poster, videoSourceAsset, videoHlsMasterAssetId);


        return ContentEditResponseDto.from(content);
    }

    public void deleteContent(String email, Long contentId) {
        User user = getUser(email);
        Content content = getContent(contentId);
        validateOwner(user, content);

        contentRepository.deleteById(contentId);
    }

    private void validateAssetType(MediaAsset asset, AssetType expected) {
        if (asset.getAssetType() != expected) {
            throw new IllegalArgumentException(
                    "자산 타입이 올바르지 않습니다. expected=" + expected + ", actual=" + asset.getAssetType());
        }
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(()-> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));
    }

    private Content getContent(Long contentId) {
        return contentRepository.findById(contentId)
                .orElseThrow(()-> new IllegalArgumentException("해당 영화가 존재하지 않습니다."));
    }

    private void validateOwner(User user, Content content) {
        if(!user.getUserId().equals(content.getOwner().getUserId()))
            throw new AccessDeniedException("접근 권한이 없습니다.");
    }
    private MediaAsset getAssetOrThrow(Long assetId, String notFoundMsg) {
        if (assetId == null) {
            throw new IllegalArgumentException("assetId는 null일 수 없습니다.");
        }
        return mediaAssetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException(notFoundMsg));
    }

    private MediaAsset getAssetOrThrow(Long assetId, AssetType expectedType, String notFoundMsg) {
        MediaAsset asset = getAssetOrThrow(assetId, notFoundMsg);
        validateAssetType(asset, expectedType);
        return asset;
    }
}
