package com.example.cinema.service.content;


import com.example.cinema.dto.common.PageResponse;
import com.example.cinema.dto.content.ContentSearchRequest;
import com.example.cinema.dto.content.ContentSearchResponse;
import com.example.cinema.repository.content.ContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentService {
    private final ContentRepository contentRepository;

    public PageResponse<ContentSearchResponse> search(ContentSearchRequest request) {
        var page = contentRepository.searchContent(request);

        return PageResponse.from(page.map(ContentSearchResponse::from));
    }

}
