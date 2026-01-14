package com.example.cinema.repository.content;

import com.example.cinema.dto.content.ContentSearchRequest;
import com.example.cinema.entity.Content;
import com.example.cinema.entity.Tag;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class ContentRepositoryTest {

    @Autowired
    ContentRepository contentRepository;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    void search() throws  Exception {

        String json = """
            {
              "page": 0,
              "size": 15,
              "title": true,
              "filter": true,
              "tags": ["Sci-Fi", "Action", "Drama"],
              "sort": "CREATED",
              "asc": false
            }
            """;

        ContentSearchRequest request = objectMapper.readValue(json, ContentSearchRequest.class);

        Page<Content> contentPage = contentRepository.searchContent(request);

        // 3. Then: 결과 검증 (AssertJ 사용 권장)
        assertThat(contentPage).isNotNull();

        // 요청한 사이즈대로 페이징 설정이 되었는가?
        assertThat(contentPage.getSize()).isEqualTo(15);

        // 실제 데이터가 조건에 맞게 필터링되어 돌아왔는가?
        // (DB에 해당 태그를 가진 데이터가 15개 이상 있다고 가정할 때)
        assertThat(contentPage.getContent()).hasSize(15);



        for (Content content : contentPage.getContent()) {
            List<String> tags = contentRepository.getTagsByContentId(content.getContentId())
                    .stream()
                    .map(Tag::getName)
                    .toList();

            System.out.println(content.getTitle() + " : " + tags);

            System.out.println("----------------------------\n");
        }




    }

}