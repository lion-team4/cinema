package com.example.cinema.repository.content;


import com.example.cinema.entity.Content;
import com.example.cinema.entity.Tag;
import com.example.cinema.repository.content.custom.ContentRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.time.LocalDateTime;

@Repository
public interface ContentRepository extends JpaRepository<Content,Long>, ContentRepositoryCustom {

    @Query("SELECT tm.tag FROM TagMap tm JOIN tm.tag WHERE tm.content.contentId = :contentId")
    List<Tag> getTagsByContentId(Long contentId);


    @Modifying
    @Query("""
           delete from Content c
           where c.status = com.example.cinema.type.ContentStatus.DRAFT
             and c.createdAt < :cutoff
           """)
    int deleteOldDrafts(@Param("cutoff") LocalDateTime cutoff);

}
