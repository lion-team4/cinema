package com.example.cinema.dto.tag;

import com.example.cinema.entity.Tag;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TagResponse {
    private Long tagId;
    private String name;

    public static TagResponse form(Tag tag){
        if (tag == null) return new TagResponse();
        return new TagResponse(tag.getTagId(), tag.getName());
    }
}
