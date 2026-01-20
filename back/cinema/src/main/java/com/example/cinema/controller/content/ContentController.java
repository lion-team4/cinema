package com.example.cinema.controller.content;

import com.example.cinema.config.common.CustomUserDetails;
import com.example.cinema.dto.common.ApiResponse;
import com.example.cinema.dto.common.PageResponse;
import com.example.cinema.dto.content.*;
import com.example.cinema.dto.tag.TagCreateRequest;
import com.example.cinema.dto.tag.TagResponse;
import com.example.cinema.entity.Content;
import com.example.cinema.service.content.ContentService;
import com.example.cinema.service.tag.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/contents")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;
    private final TagService tagService;

    @Value("${aws.cloudfront.domain}")
    private String cfDomain;

    // 1차 등록
    @PostMapping
    public ResponseEntity<ApiResponse<ContentResponse>> createContent(
            @Valid @RequestBody ContentRequest contentRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ContentResponse contentResponse = contentService.createContent(contentRequest, userDetails.getUser());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("콘텐츠가 생성되었습니다.", contentResponse));
    }

    // 2차 등록 (에셋 추가)
    @PatchMapping("/{contentId}")
    public ResponseEntity<ApiResponse<ContentResponse>> patchContent(
            @Valid @RequestBody ContentAssetAttachRequest assetAttachRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long contentId
    ) {
        ContentResponse contentResponse = contentService.addAssetsContent(assetAttachRequest, userDetails.getUser(), contentId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("콘텐츠 에셋이 추가되었습니다.", contentResponse));
    }

    // 수정 폼 조회
    @GetMapping("/{contentId}/edit")
    public ResponseEntity<ApiResponse<ContentEditResponse>> getEditForm(
            @PathVariable Long contentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ContentEditResponse editResponse = contentService.getEditContent(userDetails.getUser(), contentId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("콘텐츠 수정 정보를 조회했습니다.", editResponse));
    }

    // 콘텐츠 수정
    @PutMapping("/{contentId}")
    public ResponseEntity<ApiResponse<ContentEditResponse>> update(
            @PathVariable Long contentId,
            @Valid @RequestBody ContentUpdateRequest updateRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ContentEditResponse editResponse = contentService.updateContent(userDetails.getUser(), contentId, updateRequest);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("콘텐츠가 수정되었습니다.", editResponse));
    }

    // 콘텐츠 삭제
    @DeleteMapping("/{contentId}")
    public ResponseEntity<ApiResponse<Void>> deleteContent(
            @PathVariable Long contentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        contentService.deleteContent(userDetails.getUser(), contentId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("콘텐츠가 삭제되었습니다."));
    }

    // 인코딩 상태 조회
    @GetMapping("/{contentId}/encoding-status")
    public ResponseEntity<ApiResponse<ContentEncodingStatusResponse>> getEncodingStatus(
            @PathVariable Long contentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ContentEncodingStatusResponse response = contentService.getEncodingStatus(userDetails.getUser(), contentId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("인코딩 상태를 조회했습니다.", response));
    }

    // 콘텐츠 조회(nickname 필드 채우면 유저 한정)
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ContentSearchResponse>>>search(@ModelAttribute ContentSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success("콘텐츠 검색 성공", contentService.search(request)));
    }

    // 콘텐츠 상세 조회 (공개 콘텐츠)
    @GetMapping("/{contentId}")
    public ResponseEntity<ApiResponse<ContentDetailResponse>> getDetail(@PathVariable Long contentId) {
        Content content = contentService.getContentDetail(contentId);
        ContentDetailResponse response = ContentDetailResponse.from(content, cfDomain);
        return ResponseEntity.ok(ApiResponse.success("콘텐츠 상세 조회 성공", response));
    }

    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<TagResponse>> getTags(@RequestParam String name) {
        TagResponse response = tagService.getTag(name);
        return ResponseEntity.ok(ApiResponse.success("태그 검색 성공", response));
    }

    @PostMapping("/tags")
    public ResponseEntity<ApiResponse<TagResponse>> addTags(@RequestBody TagCreateRequest tagCreateRequest) {
        TagResponse response = tagService.addTag(tagCreateRequest);
        return ResponseEntity.ok(ApiResponse.success("태그 생성 성공", response));
    }
}
