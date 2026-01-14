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

import static org.assertj.core.api.Assertions.assertThat;



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


        assertThat(contentPage).isNotNull();


        assertThat(contentPage.getSize()).isEqualTo(15);


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