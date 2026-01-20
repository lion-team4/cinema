package com.example.cinema.entity;

import com.example.cinema.dto.tag.TagCreateRequest;
import com.example.cinema.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "tags")
public class Tag extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long tagId;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "tag", fetch = FetchType.LAZY)
    @Builder.Default
    private List<TagMap> tagMaps = new ArrayList<>();

    public static Tag create (TagCreateRequest tagCreateRequest) {
        return Tag.builder()
                .name(tagCreateRequest.getName())
                .build();
    }
}
