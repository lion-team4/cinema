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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/contents")
@RequiredArgsConstructor
public class ContentController {
    public final ContentService contentService;


    //1차등록
    @PostMapping
    public ResponseEntity<ContentResponseDto> createContent(@Valid  @RequestBody ContentRequestDto contentDto,
                                                    Principal principal){

        String email = principal.getName();
        ContentResponseDto contentResponseDto =
                contentService.createContent(contentDto, email);

        return ResponseEntity.status(HttpStatus.CREATED).body(contentResponseDto);
    }

    //2차등록
    // 2차등록이 끝나면 status만 보내도 되지 않을까요? 굳이 Dto를 보낼필요가 있을까?
    @PatchMapping("/{contentId}")
    public ResponseEntity<ContentResponseDto> patchContent( @Valid @RequestBody ContentAssetAttachRequest assertDto,
                                                    Principal principal,
                                                    @PathVariable Long contentId){

        String email = principal.getName();
        ContentResponseDto contentResponseDto = contentService.addAssetsContent(assertDto, email, contentId);

        return ResponseEntity.status(HttpStatus.OK).body(contentResponseDto);
    }

    @GetMapping("/{contentId}/edit")
    public ResponseEntity<ContentEditResponseDto> getEditForm(@PathVariable Long contentId,
                                                       Principal principal) {
        String email = principal.getName();
        ContentEditResponseDto responseDto = contentService.getEditContent(email, contentId);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @PutMapping("/{contentId}")
    public ResponseEntity<ContentEditResponseDto> update(@PathVariable Long contentId,
                                                         @Valid @RequestBody ContentUpdateRequestDto request,
                                                         Principal principal) {

        String email = principal.getName();
        ContentEditResponseDto responseDto = contentService.updateContent(email, contentId, request);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @DeleteMapping("{contentId}")
    public ResponseEntity<Void> deleteContent(@PathVariable Long contentId, Principal principal) {
        String email = principal.getName();
        contentService.deleteContent(email, contentId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
