package com.example.cinema.repository.content.custom;

import com.example.cinema.dto.content.ContentSearchRequest;
import com.example.cinema.entity.Content;
import org.springframework.data.domain.Page;

import javax.xml.stream.events.Comment;

public interface ContentRepositoryCustom {
    Page<Content> searchContent(ContentSearchRequest request);
}
