package com.example.cinema.service.content;


import com.example.cinema.dto.content.*;

public interface ContentService {

    ContentResponseDto createContent(ContentRequestDto requestDto, String email);

    ContentResponseDto addAssetsContent(ContentAssetAttachRequest assetAttachRequest, String email, Long contentId);

    ContentEditResponseDto getEditContent(String email, Long contentId);

    ContentEditResponseDto updateContent(String email, Long contentId, ContentUpdateRequestDto updateRequestDto);

    void deleteContent(String email, Long contentId);

}
