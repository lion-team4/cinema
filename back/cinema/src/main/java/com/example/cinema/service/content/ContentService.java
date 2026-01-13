package com.example.cinema.service.content;


import com.example.cinema.dto.content.*;
import com.example.cinema.entity.Content;
import com.example.cinema.entity.User;
import com.example.cinema.repository.MediaAssetRepository;
import com.example.cinema.repository.content.ContentRepository;
import com.example.cinema.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ContentService {
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final MediaAssetRepository mediaAssetRepository;

    @Transactional
    public ContentResponseDto createContent(ContentRequestDto requestDto, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        //유저 권한 확인
        if (!user.getSeller()) {
            throw new AccessDeniedException("감독 등록 후에 이용가능합니다.");
        }

        Content content = contentRepository.save(
                new Content(user, requestDto.title(), requestDto.description()));

        //이상태에서 오래되면 삭제되게..

        return ContentResponseDto.from(content);
    }

    @Transactional
    public ContentResponseDto addAssetsContent(
                ContentAssetAttachRequest assetDto,
                String ownerName,
                Long contentId) {

        //컨텐츠 저장 확인
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new NoSuchElementException("영화가 제대로 등록되지 않았습니다."));

        //MediaAsset 을 dto에서 추출
        ContentResponseDto responseDto = ContentResponseDto.from(content);

        //MediaAsset을 content에 등록
//        ContentResponseDto responseDto = new ContentResponseDto(content.attachAssets());

        //mediaAsset이 null인지 검증 로직


        return responseDto;
    }

    @Transactional(readOnly = true)
    public ContentEditResponseDto getEditContent(String email, Long contentId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new IllegalArgumentException("해당 유저가 존재하지 않습니다"));

        Content content = contentRepository.findById(contentId)
                .orElseThrow(()-> new IllegalArgumentException("해당 영화가 존재하지 않습니다."));

        //유저 권한 확인
        if(!user.getUserId().equals(content.getOwner().getUserId())) {
            throw new AccessDeniedException("조회권한이 없습니다");
        }

        return ContentEditResponseDto.from(content);
    }

    @Transactional
    public ContentEditResponseDto updateContent(String email,
                                                Long contentId,
                                                ContentUpdateRequestDto updateRequestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new IllegalArgumentException("해당 유저가 존재하지 않습니다"));

        Content content = contentRepository.findById(contentId)
                .orElseThrow(()-> new IllegalArgumentException("해당 영화가 존재하지 않습니다."));

        //유저 권한 확인
        if(!user.getUserId().equals(content.getOwner().getUserId())) {
            throw new AccessDeniedException("조회권한이 없습니다");
        }

        //updateRequestDto -> mediaAssets 추출 후 조회

        //기본 정보 수정
        content.updateInfo(updateRequestDto.title(), updateRequestDto.description(), updateRequestDto.status());

        //mediaAssets 수정
//        content.attachAssets();


        return ContentEditResponseDto.from(content);
    }

    public void deleteContent(String email, Long contentId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new IllegalArgumentException("해당 유저가 존재하지 않습니다"));

        Content content = contentRepository.findById(contentId)
                .orElseThrow(()-> new IllegalArgumentException("해당 영화가 존재하지 않습니다."));

        //유저 권한 확인
        if(!user.getUserId().equals(content.getOwner().getUserId())) {
            throw new AccessDeniedException("접근권한이 없습니다");
        }

        contentRepository.deleteById(contentId);
    }
}
