package com.example.cinema.service.content;


import com.example.cinema.dto.content.*;

public interface ContentService {

    ContentResponseDto createContent(ContentRequestDto requestDto, String ownerName);

    ContentResponseDto addAssetsContent(ContentAssetAttachRequest assetAttachRequest, String ownerName, Long contentId);

    ContentEditResponseDto getEditContent(String ownerName, Long contentId);

    ContentEditResponseDto updateContent(String ownerName, Long contentId, ContentUpdateRequestDto updateRequestDto);


}
