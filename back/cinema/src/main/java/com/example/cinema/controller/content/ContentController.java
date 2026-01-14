package com.example.cinema.controller.content;


import com.example.cinema.dto.common.ApiResponse;
import com.example.cinema.dto.common.PageResponse;
import com.example.cinema.dto.content.ContentSearchRequest;
import com.example.cinema.dto.content.ContentSearchResponse;
import com.example.cinema.service.content.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contents")
@RequiredArgsConstructor
public class ContentController {
    private final ContentService contentService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ContentSearchResponse>>>search(@ModelAttribute ContentSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success("콘텐츠 검색 성공", contentService.search(request)));
    }
}
