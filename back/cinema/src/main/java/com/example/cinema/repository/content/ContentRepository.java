package com.example.cinema.repository.content;


import com.example.cinema.entity.Content;
import com.example.cinema.entity.Tag;
import com.example.cinema.repository.content.custom.ContentRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentRepository extends JpaRepository<Content,Long>, ContentRepositoryCustom {

    @Query("SELECT tm.tag FROM TagMap tm JOIN tm.tag WHERE tm.content.contentId = :contentId")
    List<Tag> getTagsByContentId(Long contentId);
}
