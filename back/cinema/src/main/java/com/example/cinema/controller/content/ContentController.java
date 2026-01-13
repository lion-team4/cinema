package com.example.cinema.controller.content;


import com.example.cinema.dto.content.*;
import com.example.cinema.service.content.ContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController("/api")
@RequiredArgsConstructor
public class ContentController {
    public final ContentService contentService;


    //1차등록
    @PostMapping("/contents")
    public ResponseEntity<ContentResponseDto> createContent(
            @Valid  @RequestBody ContentRequestDto contentDto,
            Principal principal){

        String ownerName = principal.getName();
        ContentResponseDto contentResponseDto =
                contentService.createContent(contentDto, ownerName);

        return ResponseEntity.status(HttpStatus.CREATED).body(contentResponseDto);
    }

    //2차등록
    // 2차등록이 끝나면 status만 보내도 되지 않을까요? 굳이 Dto를 보낼필요가 있을까?
    @PatchMapping("/contents/{id}")
    public ResponseEntity<ContentResponseDto> patchContent(
            @Valid @RequestBody ContentAssetAttachRequest assertDto,
            Principal principal,
            @PathVariable Long id){
        String ownerName = principal.getName();
        contentService.addAssetsContent(assertDto, ownerName, id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{contentId}/edit")
    public ResponseEntity<ContentEditResponseDto> getEditForm(@PathVariable Long contentId,
                                                       Principal principal) {
        String ownerName = principal.getName();
        ContentEditResponseDto responseDto = contentService.getEditContent(ownerName, contentId);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @PutMapping("/{contentId}")
    public ResponseEntity<ContentEditResponseDto> update(@PathVariable Long contentId,
                                                         @Valid @RequestBody ContentUpdateRequestDto request,
                                                         Principal principal) {

        String ownerName = principal.getName();
        ContentEditResponseDto responseDto = contentService.updateContent(ownerName, contentId, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
