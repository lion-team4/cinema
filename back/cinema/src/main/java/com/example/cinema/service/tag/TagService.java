package com.example.cinema.service.tag;

import com.example.cinema.dto.tag.TagCreateRequest;
import com.example.cinema.dto.tag.TagResponse;
import com.example.cinema.entity.Tag;
import com.example.cinema.repository.tag.TagRepository;
import com.example.cinema.exception.TagDuplicateException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    public TagResponse getTag(String name) {
        Tag tag = tagRepository.findByName(name)
                .orElse(null);

        return TagResponse.form(tag);
    }

    @Transactional
    public TagResponse addTag(TagCreateRequest tagCreateRequest) {
        if (tagRepository.existsByName(tagCreateRequest.getName())) {
            throw new TagDuplicateException();
        }
        Tag tag = tagRepository.save(Tag.create(tagCreateRequest));
        return TagResponse.form(tag);
    }

}
