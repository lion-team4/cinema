package com.example.cinema.repository.content;


import com.example.cinema.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ContentRepository extends JpaRepository<Content,Long> {

    @Modifying
    @Query("""
           delete from Content c
           where c.status = com.example.cinema.type.ContentStatus.DRAFT
             and c.createdAt < :cutoff
           """)
    int deleteOldDrafts(@Param("cutoff") LocalDateTime cutoff);

}
