package com.example.cinema.controller.content;

import com.example.cinema.dto.common.ApiResponse;
import com.example.cinema.dto.common.PageResponse;
import com.example.cinema.dto.content.*;
import com.example.cinema.service.content.ContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/contents")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    // 1차 등록
    @PostMapping
    public ResponseEntity<ApiResponse<ContentResponse>> createContent(
            @Valid @RequestBody ContentRequest contentRequest,
            Principal principal
    ) {
        String email = principal.getName();
        ContentResponse contentResponse = contentService.createContent(contentRequest, email);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("콘텐츠가 생성되었습니다.", contentResponse));
    }

    // 2차 등록 (에셋 추가)
    @PatchMapping("/{contentId}")
    public ResponseEntity<ApiResponse<ContentResponse>> patchContent(
            @Valid @RequestBody ContentAssetAttachRequest assetAttachRequest,
            Principal principal,
            @PathVariable Long contentId
    ) {
        String email = principal.getName();
        ContentResponse contentResponse = contentService.addAssetsContent(assetAttachRequest, email, contentId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("콘텐츠 에셋이 추가되었습니다.", contentResponse));
    }

    // 수정 폼 조회
    @GetMapping("/{contentId}/edit")
    public ResponseEntity<ApiResponse<ContentEditResponse>> getEditForm(
            @PathVariable Long contentId,
            Principal principal
    ) {
        String email = principal.getName();
        ContentEditResponse editResponse = contentService.getEditContent(email, contentId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("콘텐츠 수정 정보를 조회했습니다.", editResponse));
    }

    // 콘텐츠 수정
    @PutMapping("/{contentId}")
    public ResponseEntity<ApiResponse<ContentEditResponse>> update(
            @PathVariable Long contentId,
            @Valid @RequestBody ContentUpdateRequest updateRequest,
            Principal principal
    ) {
        String email = principal.getName();
        ContentEditResponse editResponse = contentService.updateContent(email, contentId, updateRequest);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("콘텐츠가 수정되었습니다.", editResponse));
    }

    // 콘텐츠 삭제
    @DeleteMapping("/{contentId}")
    public ResponseEntity<ApiResponse<Void>> deleteContent(
            @PathVariable Long contentId,
            Principal principal
    ) {
        String email = principal.getName();
        contentService.deleteContent(email, contentId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("콘텐츠가 삭제되었습니다."));
    }

    // 콘텐츠 조회(nickname 필드 채우면 유저 한정)
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ContentSearchResponse>>>search(@ModelAttribute ContentSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success("콘텐츠 검색 성공", contentService.search(request)));
    }
}